package cn.iot.things.strategy.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class TestConfig {
    @PostConstruct
    public void init() {
        log.info("=== 用户模块配置初始化 ===");
    }

    @Bean
    public String userModuleBean() {
        log.info("=== 用户模块Bean创建 ===");
        return "user-module-active";
    }
}
