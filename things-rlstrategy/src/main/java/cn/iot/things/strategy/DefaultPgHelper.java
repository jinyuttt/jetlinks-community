
package cn.iot.things.strategy;

import cn.iot.things.strategy.configuration.PgProperties;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


/**
 * 操作接口
 * 就是平台的oprations
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultPgHelper implements CustomHelper, ApplicationContextAware, CommandLineRunner {

    private  PgProperties properties;
    private final Disposable.Composite disposable = Disposables.composite();

    private ApplicationContext context;

    private DatabaseClient databaseClient=null;

    Map<String,String> map=new HashMap<>();//数据库类型

    ConcurrentHashMap<String,List<String>> mapBuf=new ConcurrentHashMap<>();

    ConcurrentHashMap<String,List<String>> mapColmunIndex =new ConcurrentHashMap<>();

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmm");

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
           process();
           deleteCsv();
           if(properties.isCsv()&&properties.getCopycmd().isEmpty()) {
               log.warn("没有配置copy命令，自动转换批量SQL");
               if (!properties.isBatch()) {
                   properties.setBatch(true);
               }
           }
           //默认"
           if(properties.isCsv()&&(properties.getSplt()==null||properties.getSplt().isEmpty())) {
               properties.setSplt("\"");
           }
    }


    /**
     * 定时处理数据
     */
    private void process(){
        Thread ss=new Thread(()->{
            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!mapColmunIndex.isEmpty()) {
                    //
                    if (!mapBuf.isEmpty()) {
                        //
                        if (properties.isBatch()) {
                            //批量入库
                            for (Map.Entry<String, List<String>> entry : mapBuf.entrySet()) {
                                try {
                                    StringBuffer sql = new StringBuffer();
                                    sql.append("insert into ").append(entry.getKey()).append(" (");
                                    List<String> list = mapColmunIndex.get(entry.getKey());
                                    if (entry.getValue() == null || entry.getValue().isEmpty()) {
                                        continue;
                                    }
                                    sql.append(String.join(",", list));
                                    sql.append(") values ");
                                    sql.append(String.join(",", entry.getValue()));
                                    entry.getValue().clear();
                                    databaseClient.sql(sql.toString()).fetch().rowsUpdated().block();
                                }catch (Exception e){
                                    log.error("自定义行存储批量sql",e);
                                }
                            }


                        }
                     else    if (properties.isCsv()) {
                            for (Map.Entry<String, List<String>> entry : mapBuf.entrySet()) {
                                try {
                                    if(entry.getValue() == null || entry.getValue().isEmpty()) {
                                        continue;
                                    }
                                  String file=  writeCsv(entry.getKey(), entry.getValue());
                                    entry.getValue().clear();
                                    String cmd=String.format(properties.getCopycmd(),entry.getKey(),file);
                                    databaseClient.sql(cmd).fetch().rowsUpdated().block();
                                }catch (Exception e){
                                    log.error("自定义行存储csv",e);
                                }
                            }
                        }
                    }
                }
            }
        });
        ss.setDaemon(true);
        ss.setName("Insert db");
        ss.start();
    }

    /**
     * 递归删除文件夹及其所有内容
     */
    public static boolean deleteFolder(File folder) {
        if (folder == null || !folder.exists()) {
            return true;
        }

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 递归删除子文件和子文件夹
                    deleteFolder(file);
                }
            }
        }

        // 删除空文件夹或文件
        return folder.delete();
    }


    /**
     * 删除csv
     */
    private void deleteCsv(){
        Thread del=new Thread(()->{
            if(!properties.isCsv()){
                return;
            }
           if(properties.isBatch()){
               return;
           }
           SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
           while (true){
               try {
                   Thread.sleep(60*60*1000);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
               File dir=new File(properties.getCsvdir());
               if(dir.exists()){
                String day=   sdf.format(new Date());
                long cur=Long.valueOf(day)*100*100;
                   File[] subfolders = dir.listFiles(File::isDirectory);
                   if (subfolders != null) {
                       for (File subfolder : subfolders) {
                          // System.out.println("子文件夹: " + subfolder.getName());
                           try{
                               long last=Long.valueOf(subfolder.getName());
                               if(last<cur){
                                   //非当天数据删除
                                   deleteFolder(subfolder);
                               }
                           }catch (Exception e){

                           }
                       }
                   }
               }
           }
        });
        del.setName("Deletecsv");
        del.setDaemon(true);
        del.start();
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


    /**
     * 写入
     * @param table
     * @param data
     * @return
     */
    private String writeCsv(String table,List<String> data) {
        String d = sdf.format(new Date());
        String dir = Paths.get(properties.getCsvdir(), d).toAbsolutePath().toString();
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = Paths.get(dir, table + "_" + UUID.randomUUID().toString() + ".csv").toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder sb = new StringBuilder();

            for (String line : data) {
                sb.append(line).append("\n");
            }
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
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

         List<String> lst=new ArrayList<>();
        for (var propertyMetadata : collect) {
            lst.add(propertyMetadata.getId());
            String colType=map.getOrDefault(propertyMetadata.getValueType().getType(),propertyMetadata.getValueType().getType());
            stringBuilder.append(propertyMetadata.getId()).append(" ").append(colType).append(" ,");
            stringBuffer.append("COMMENT ON COLUMN").append(" "+tableName+".").append(propertyMetadata.getId()).append(" IS '").append(propertyMetadata.getName()).append("';");
        }
        mapColmunIndex.put(tableName,lst);//保存表列顺序
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
         stringBuilder.append(")");
        databaseClient.sql(stringBuilder.toString().toLowerCase()).then().block();
        databaseClient.sql(stringBuffer.toString().toLowerCase()).then().block();
        return Mono.empty();
    }

    @Override
    public Mono<Void> doSave(String metric, TimeSeriesData data) {
        if(properties.isBatch()){
            //
            if(mapColmunIndex.containsKey(metric)) {
                List<String> list = mapColmunIndex.get(metric);
                StringBuffer buffer = new StringBuffer();
                buffer.append("(");
                for (String col : list) {
                    Object obj = data.getData().getOrDefault(col, null);
                    buffer.append("'").append(obj).append("',");
                }
                buffer.deleteCharAt(buffer.length() - 1);
                buffer.append(")");
                List<String> bufData = mapBuf.getOrDefault(metric, null);
                if (bufData == null) {
                    bufData = new ArrayList<>();
                    mapBuf.put(metric, bufData);
                }
                bufData.add(buffer.toString());

                if(bufData.size()>properties.getBatchNum()){
                    //批量入库
                    StringBuffer sql=new StringBuffer();
                    sql.append("insert into ").append(metric).append(" (");
                    sql.append(String.join(",",list));
                    sql.append(") values ");
                    sql.append(String.join(",",bufData));
                    bufData.clear();
                    return databaseClient.sql(sql.toString().toLowerCase()).then();
                }
                return Mono.empty();
            }
        }
       else if(properties.isCsv()){
            if(mapColmunIndex.containsKey(metric)) {
                List<String> list = mapColmunIndex.get(metric);
                StringBuffer buffer = new StringBuffer();
                for (String col : list) {
                    Object obj = data.getData().getOrDefault(col, null);
                    if(obj!=null&&obj.getClass().isAssignableFrom(String.class)) {
                        if(obj.toString().indexOf(",")!=-1){
                            String content=obj.toString();
                            content=content.replace(properties.getSplt(),properties.getSplt()+properties.getSplt());
                            content=properties.getSplt()+content+properties.getSplt();
                            obj=content;
                        }
                    }
                    buffer.append(obj).append(",");
                }
                buffer.deleteCharAt(buffer.length() - 1);
                List<String> bufData = mapBuf.getOrDefault(metric, null);
                if (bufData == null) {
                    bufData = new ArrayList<>();
                    mapBuf.put(metric, bufData);
                }
                bufData.add(buffer.toString());
                if(bufData.size()>properties.getBatchNum()){
                   String file= writeCsv(metric,bufData);
                   String cmd=String.format(properties.getCopycmd(), metric, file);
                    databaseClient.sql(cmd).fetch().rowsUpdated().then();
                    bufData.clear();
                    return Mono.empty();
                }
                return Mono.empty();
            }
        }

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
        return databaseClient.sql(stringBuilder.toString().toLowerCase()).fetch().rowsUpdated().then();
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
        List<String> lst=new ArrayList<>();
        for (PropertyMetadata propertyMetadata : collect) {
//            if(propertyMetadata.getId().toLowerCase().equals("id")){
//                continue;
//            }
            String colType=map.getOrDefault(propertyMetadata.getValueType().getType(),propertyMetadata.getValueType().getType());
            stringBuilder.append(propertyMetadata.getId()).append(" ").append(colType).append(" ,");
            stringBuffer.append("COMMENT ON COLUMN").append(" "+tableName+".").append(propertyMetadata.getId()).append(" IS '").append(propertyMetadata.getName()).append("';");
              lst.add(propertyMetadata.getId());
        }
       // stringBuilder.append("content text ");
      //  stringBuilder.append(")");
        mapColmunIndex.put(tableName,lst);
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
