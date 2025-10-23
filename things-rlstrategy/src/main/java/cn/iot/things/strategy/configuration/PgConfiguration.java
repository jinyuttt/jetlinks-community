package cn.iot.things.strategy.configuration;

import cn.iot.things.strategy.DefaultPgHelper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


/**
 * 配置postgresql存储启用
 * 有redis连接配置，通过连接属性启用
 *
 *
 */
@AutoConfiguration
@EnableConfigurationProperties(PgProperties.class)
@ConditionalOnProperty(prefix = "rldb", name = "enabled", havingValue = "true")
public class PgConfiguration {
    @Bean(destroyMethod = "shutdown",initMethod = "init")
    public DefaultPgHelper pgOperations(PgProperties properties) {
        return new DefaultPgHelper(properties);
    }


}
