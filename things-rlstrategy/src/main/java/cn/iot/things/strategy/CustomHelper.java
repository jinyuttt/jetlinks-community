package cn.iot.things.strategy;

import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.jetlinks.community.timeseries.TimeSeriesData;
import org.jetlinks.core.metadata.PropertyMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public interface CustomHelper {
    Mono<Void> doSave(String metric, TimeSeriesData data);
    Mono<Void> doSave(String metric, Flux<TimeSeriesData> data);

    Flux<TimeSeriesData> doQuery(String metric, QueryParamEntity param);

    <T> Mono<PagerResult<T>> doQueryPager(String metric, QueryParamEntity param, Function<TimeSeriesData,T> mapper);

    Mono<Void> createTable(String metric, List<PropertyMetadata> properties, String thingIdProperty, String columnTimestamp);

    Mono<Void> reload(String metric);

    Mono<Void> createTable(String metric, List<PropertyMetadata> properties, String thingIdProperty, String columnEventId, String columnTimestamp);
}
