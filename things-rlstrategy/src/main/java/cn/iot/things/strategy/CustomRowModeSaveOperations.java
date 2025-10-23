package cn.iot.things.strategy;

import org.jetlinks.community.things.data.operations.DataSettings;
import org.jetlinks.community.things.data.operations.MetricBuilder;
import org.jetlinks.community.things.data.operations.RowModeSaveOperationsBase;
import org.jetlinks.community.timeseries.TimeSeriesData;
import org.jetlinks.core.message.ThingMessage;
import org.jetlinks.core.message.event.ThingEventMessage;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.things.ThingMetadata;
import org.jetlinks.core.things.ThingsRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

//创建自定义SaveOperations

public class CustomRowModeSaveOperations extends RowModeSaveOperationsBase {

    private final CustomHelper helper;


    public CustomRowModeSaveOperations(ThingsRegistry registry,
                                       MetricBuilder metricBuilder,
                                       DataSettings settings,
                                       CustomHelper helper) {
        super(registry, metricBuilder, settings);
        this.helper = helper;
    }


    @Override
    protected Map<String, Object> createRowPropertyData(String id, long timestamp, ThingMessage message, PropertyMetadata property, Object value) {
        Map<String, Object> values = super.createRowPropertyData(id, timestamp, message, property, value);
        //可能存在不同的数据库对于数据类型的处理差异，values为物模型相关信息如属性id、设备id、时间戳、数据id等。
        //此处可以做一些针对不同存储时数据类型兼容或转换的处理

        return values;
    }

    @Override
    protected Map<String, Object> createEventData(ThingEventMessage message, ThingMetadata metadata) {
        //伪代码，不同的数据库对于数据类型的处理存在差异，可以自行在此处处理事件内的数据类型
      return   super.createEventData(message, metadata);
       // return doSomeThing(message, metadata);
    }

    private Map<String, Object> doSomeThing(ThingEventMessage message, ThingMetadata metadata) {
        return  metadata.getExpands();
    }

    @Override
    protected Mono<Void> doSave(String metric, TimeSeriesData data) {
        return helper.doSave(metric, data);
    }



    @Override
    protected Mono<Void> doSave(String metric, Flux<TimeSeriesData> data) {
        return helper.doSave(metric, data);
    }


}
