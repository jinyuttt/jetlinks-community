package cn.iot.things.strategy;

import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.jetlinks.community.things.data.*;
import org.jetlinks.community.things.data.operations.*;
import org.jetlinks.community.timeseries.TimeSeriesData;
import org.jetlinks.community.timeseries.query.AggregationData;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.things.ThingMetadata;
import org.jetlinks.core.things.ThingsRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

//创建自定义QueryOperations
//伪代码，根据自己的需求在此处实现query操作实现
public class CustomModeQueryOperations extends RowModeQueryOperationsBase {

    private final CustomHelper helper;

    public CustomModeQueryOperations(String thingType,
                                     String thingTemplateId,
                                     String thingId,
                                     MetricBuilder metricBuilder,
                                     DataSettings settings,
                                     ThingsRegistry registry,
                                     CustomHelper helper) {
        super(thingType, thingTemplateId, thingId, metricBuilder, settings, registry);
        this.helper = helper;
    }

    @Override
    protected Flux<TimeSeriesData> doQuery(String metric, Query<?, QueryParamEntity> query) {
        return helper.doQuery(metric, query.getParam());
    }

    @Override
    protected <T> Mono<PagerResult<T>> doQueryPage(String metric, Query<?, QueryParamEntity> query, Function<TimeSeriesData, T> mapper) {
        return helper.doQueryPager(metric, query.getParam(), mapper);
    }

    @Override
    protected Flux<AggregationData> doAggregation(String metric,
                                                  AggregationRequest request,
                                                  AggregationContext context) {
        //此处伪代码，需自行在aggregationQuery内编写聚合查询实现
        return aggregationQuery();
    }

    private Flux<AggregationData> aggregationQuery() {
        return  null;
    }

    @Override
    protected Flux<ThingPropertyDetail> queryEachProperty(@Nonnull String metric,
                                                          @Nonnull Query<?, QueryParamEntity> query,
                                                          @Nonnull ThingMetadata metadata,
                                                          @Nonnull Map<String, PropertyMetadata> properties) {
        //此处伪代码，需自行在doSomeThings内编写查询实现
        return doSomeThings();
    }

    private Flux<ThingPropertyDetail> doSomeThings() {
        return  null;
    }


}
