
package cn.iot.things.strategy;

import cn.iot.things.strategy.configuration.PgProperties;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.jetlinks.community.timeseries.TimeSeriesData;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * 操作接口
 * 就是平台的oprations
 */
@RequiredArgsConstructor
public class DefaultPgHelper implements CustomHelper, ApplicationContextAware, CommandLineRunner {

    private  PgProperties properties;
    private final Disposable.Composite disposable = Disposables.composite();

    private ApplicationContext context;

    private DatabaseClient databaseClient=null;

    Map<String,String> map=new HashMap<>();

    public DefaultPgHelper(PgProperties properties) {
        this.properties = properties;
    }


    /**
     * 关闭
     */
    public void shutdown() {
        disposable.dispose();
    }

    /**
     * 初始化数据库配置
     */
    public void init() {
        if (properties.isSharedSpring() && context != null) {
            databaseClient=  context.getBean(DatabaseClient.class);
        } else {
            if (properties.getR2dbc() == null) {
                throw new IllegalArgumentException("db.rl.r2dbc must not be null");
            }
           databaseClient=DatabaseClient.create(createConnectionPool());
        }
           map.put("string","text");
           map.put("double","decimal");
           map.put("geoPoint","point");
           map.put("date","bigint");
    }

    /**
     * 创建连接池
     */
    public   ConnectionFactory  createConnectionPool() {

        String host=properties.getR2dbc().getProperties().getOrDefault("host","");
        String port=properties.getR2dbc().getProperties().getOrDefault("port","");
        String database=properties.getR2dbc().getProperties().getOrDefault("database","");
        String username=properties.getR2dbc().getUsername();
        String password=properties.getR2dbc().getPassword();
        String url=properties.getR2dbc().getUrl();

        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                                                                   .option(ConnectionFactoryOptions.DRIVER, properties.getDb_dialect())
                                                                   .option(ConnectionFactoryOptions.HOST,host)
                                                                   .option(ConnectionFactoryOptions.PORT, Integer.parseInt(port))
                                                                   .option(ConnectionFactoryOptions.USER, username)
                                                                   .option(ConnectionFactoryOptions.PASSWORD, password)
                                                                   .option(ConnectionFactoryOptions.DATABASE, database)
                                                                   .build();
        if(StringUtils.isNotEmpty(url)){
             options=   ConnectionFactoryOptions.parse(url);
             options.mutate().option(ConnectionFactoryOptions.DRIVER, properties.getDb_dialect());
             options.mutate().option(ConnectionFactoryOptions.DATABASE, database);
             options.mutate().option(ConnectionFactoryOptions.USER, username);
             options.mutate().option(ConnectionFactoryOptions.PASSWORD, password);
        }

        ConnectionFactory connectionFactory = ConnectionFactories.get(options);
        ConnectionPoolConfiguration config = ConnectionPoolConfiguration.builder(connectionFactory)
            .initialSize(properties.getR2dbc().getPool().getInitialSize())
            .maxSize(properties.getR2dbc().getPool().getMaxSize())
            .minIdle(properties.getR2dbc().getPool().getMinIdle())
            .maxIdleTime(properties.getR2dbc().getPool().getMaxIdleTime())
            .maxLifeTime(properties.getR2dbc().getPool().getMaxLifeTime())
            .maxAcquireTime(properties.getR2dbc().getPool().getMaxAcquireTime())
            .build();

        return new ConnectionPool(config);

    }



    @Override
    public void run(String... args) throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;

    }



    @Override
    public Mono<Void> createTable(String metric, List<PropertyMetadata> collect, String thingIdProperty, String columnPropertyId, String columnTimestamp) {
        //
        StringBuilder stringBuilder=new StringBuilder();
        StringBuffer stringBuffer=new StringBuffer();
        String tableName=metric;
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ");

        stringBuilder.append(tableName);
        stringBuilder.append(" (");
       // stringBuilder.append("ID BIGINT PRIMARY KEY      NOT NULL,");
        for (var propertyMetadata : collect) {
//            if(propertyMetadata.getId().toLowerCase().equals("id")){
//                continue;
//            }
            String colType=map.getOrDefault(propertyMetadata.getValueType().getType(),propertyMetadata.getValueType().getType());

            stringBuilder.append(propertyMetadata.getId()).append(" ").append(colType).append(" ,");
            stringBuffer.append("COMMENT ON COLUMN").append(" "+tableName+".").append(propertyMetadata.getId()).append(" IS '").append(propertyMetadata.getName()).append("';");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
         stringBuilder.append(")");
        databaseClient.sql(stringBuilder.toString().toLowerCase()).then().block();
        databaseClient.sql(stringBuffer.toString().toLowerCase()).then().block();
        return Mono.empty();
    }

    @Override
    public Mono<Void> doSave(String metric, TimeSeriesData data) {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("INSERT INTO ");
        stringBuilder.append(metric);
        stringBuilder.append(" (");
        StringBuffer buffer=new StringBuffer();
        for (Map.Entry<String,Object> entry:data.getData().entrySet()) {
            stringBuilder.append(entry.getKey()).append(",");
            buffer.append("'").append(entry.getValue()).append("',");
        }
       stringBuilder.deleteCharAt(stringBuilder.length()-1);
        buffer.deleteCharAt(buffer.length()-1);
        stringBuilder.append(") VALUES (");
        stringBuilder.append(buffer.toString()).append(");");
        return databaseClient.sql(stringBuilder.toString().toLowerCase()).then();
    }

    @Override
    public Mono<Void> doSave(String metric, Flux<TimeSeriesData> data) {
      return   data.map(p->this.doSave(metric,p).block()).then();

    }

    @Override
    public Flux<TimeSeriesData> doQuery(String metric, QueryParamEntity param) {
        return null;
    }

    @Override
    public <T> Mono<PagerResult<T>> doQueryPager(String metric, QueryParamEntity param, Function<TimeSeriesData, T> mapper) {
        return null;
    }

    @Override
    public Mono<Void> createTable(String metric, List<PropertyMetadata> collect, String thingIdProperty, String columnTimestamp) {
        StringBuilder stringBuilder=new StringBuilder();
        StringBuffer stringBuffer=new StringBuffer();
        String tableName=metric;
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ");

        stringBuilder.append(tableName);
        stringBuilder.append(" (");
      //  stringBuilder.append("ID BIGINT PRIMARY KEY      NOT NULL,");
        for (var propertyMetadata : collect) {
//            if(propertyMetadata.getId().toLowerCase().equals("id")){
//                continue;
//            }
            String colType=map.getOrDefault(propertyMetadata.getValueType().getType(),propertyMetadata.getValueType().getType());
            stringBuilder.append(propertyMetadata.getId()).append(" ").append(colType).append(" ,");
            stringBuffer.append("COMMENT ON COLUMN").append(" "+tableName+".").append(propertyMetadata.getId()).append(" IS '").append(propertyMetadata.getName()).append("';");

        }
       // stringBuilder.append("content text ");
      //  stringBuilder.append(")");
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append(")");
        databaseClient.sql(stringBuilder.toString().toLowerCase()).then().block();
        databaseClient.sql(stringBuffer.toString().toLowerCase()).then().block();
        return Mono.empty();
    }

    @Override
    public Mono<Void> reload(String metric) {
        return null;
    }

}
