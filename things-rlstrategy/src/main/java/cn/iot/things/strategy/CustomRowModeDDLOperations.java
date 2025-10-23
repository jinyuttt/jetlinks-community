package cn.iot.things.strategy;

import org.hswebframework.ezorm.rdb.codec.DateTimeCodec;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.ddl.TableBuilder;
import org.jetlinks.community.things.data.ThingsDataConstants;
import org.jetlinks.community.things.data.operations.DataSettings;
import org.jetlinks.community.things.data.operations.MetricBuilder;
import org.jetlinks.community.things.data.operations.RowModeDDLOperationsBase;
import org.jetlinks.community.things.utils.ThingsDatabaseUtils;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.things.ThingsRegistry;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.sql.JDBCType;
import java.util.*;
import java.util.stream.Collectors;

//创建自定义DDLOperations
//伪代码，根据自己的需求在此处实现ddl操作实现
public class CustomRowModeDDLOperations extends RowModeDDLOperationsBase {

    private final CustomHelper helper;

    public CustomRowModeDDLOperations(String thingType,
                                      String templateId,
                                      String thingId,
                                      DataSettings settings,
                                      MetricBuilder metricBuilder,
                                      CustomHelper helper) {
        super(thingType, templateId, thingId, settings, metricBuilder);
        this.helper = helper;
    }

    //不存储的数据类型
    static Set<String> notSaveColumns = new HashSet<>(Arrays.asList(
        ThingsDataConstants.COLUMN_PROPERTY_OBJECT_VALUE,
        ThingsDataConstants.COLUMN_PROPERTY_ARRAY_VALUE,
        ThingsDataConstants.COLUMN_LOG_TYPE,
        ThingsDataConstants.COLUMN_PROPERTY_TYPE
    ));

    public CustomRowModeDDLOperations(String thingType, String templateId, String thingId, MetricBuilder metricBuilder, DataSettings settings, ThingsRegistry registry, CustomHelper helper) {
        super(thingType, templateId, thingId, settings, metricBuilder);
        this.helper = helper;
    }

    @Override
    protected Mono<Void> register(MetricType metricType, String metric, List<PropertyMetadata> properties) {
        //根据物模型在时序库内创建表结构或映射
        switch (metricType) {
            case properties:
                return helper
                    .createTable(metric, properties
                                     .stream()
                                     //过滤不存储的数据类型
                                     .filter(prop -> !notSaveColumns.contains(prop.getId()))
                                     .collect(Collectors.toList()),
                                 metricBuilder.getThingIdProperty(),
                                 ThingsDataConstants.COLUMN_PROPERTY_ID,
                                 ThingsDataConstants.COLUMN_TIMESTAMP);

            case log:
                return helper
                    .createTable(metric, properties,
                                 metricBuilder.getThingIdProperty(),
                                 ThingsDataConstants.COLUMN_TIMESTAMP);
            case event:
                if (settings.getEvent().eventIsAllInOne()) {
                    return helper
                        .createTable(metric, properties,
                                     metricBuilder.getThingIdProperty(),
                                     ThingsDataConstants.COLUMN_EVENT_ID,
                                     ThingsDataConstants.COLUMN_TIMESTAMP);

                }
                return helper
                    .createTable(metric, properties,
                                 metricBuilder.getThingIdProperty(),
                                 ThingsDataConstants.COLUMN_TIMESTAMP);
        }
        return Mono.empty();
    }

    @Override
    protected Mono<Void> reload(MetricType metricType, String metric, List<PropertyMetadata> properties) {
       // return register0(metricType, metric, properties, false);

        return helper.reload(metric);
    }

}