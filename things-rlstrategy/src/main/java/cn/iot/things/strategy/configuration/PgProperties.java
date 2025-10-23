package cn.iot.things.strategy.configuration;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rldb")
@Getter
@Setter
public class PgProperties {
    private boolean enabled = false;

    //是否共享spring容器中的连接
    //需要平台也使用timescaledb
    private boolean sharedSpring = false;

    //当sharedSpring未false时,使用此连接配置.
    private R2dbcProperties r2dbc = new R2dbcProperties();

    //数据库的schema
    private String schema = "public";

   private  String Db_dialect;

   private  String logname;

   private  String propertiesname;

}
