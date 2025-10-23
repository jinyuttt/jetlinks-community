package cn.iot.things.strategy.configuration;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.community.Interval;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rldb.things-data")
public class PgThingsDataProperties {
    private boolean enabled = true;

    /**
     * 分区时间间隔
     */
    private Interval chunkTimeInterval = Interval.ofDays(7);

    /**
     * 数据保留时长
     */
    private Interval retentionPolicy;
}
