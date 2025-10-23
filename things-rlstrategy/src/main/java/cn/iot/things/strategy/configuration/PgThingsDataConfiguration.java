/*
 * Copyright 2025 JetLinks https://www.jetlinks.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iot.things.strategy.configuration;



import cn.iot.things.strategy.CustomHelper;
import cn.iot.things.strategy.CustomRowModeStrategy;
import org.jetlinks.community.things.data.ThingsDataRepositoryStrategy;
import org.jetlinks.core.things.ThingsRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


/**
 * 序列数据存储配置注册
 * 有redis连接类型，启用配置
 * ThingsRegistry好像是延迟建立的，如果加上就不能初始化
 *
 *
 */
@AutoConfiguration(after =  PgConfiguration.class)
@ConditionalOnBean({CustomHelper.class})
@ConditionalOnClass(ThingsDataRepositoryStrategy.class)
@ConditionalOnProperty(prefix = "rldb.things-data", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PgThingsDataProperties.class)
public class PgThingsDataConfiguration {


    @Bean
    public CustomRowModeStrategy customRowModeStrategy(ThingsRegistry registry,
                                                       CustomHelper helper,
                                                       PgThingsDataProperties properties) {
        return new CustomRowModeStrategy(registry, helper);
    }




}
