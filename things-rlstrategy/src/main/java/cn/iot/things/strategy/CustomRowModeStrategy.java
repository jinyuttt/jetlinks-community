package cn.iot.things.strategy;

import lombok.AllArgsConstructor;
import org.jetlinks.community.things.data.AbstractThingDataRepositoryStrategy;
import org.jetlinks.community.things.data.operations.DDLOperations;
import org.jetlinks.community.things.data.operations.QueryOperations;
import org.jetlinks.community.things.data.operations.SaveOperations;
import org.jetlinks.core.things.ThingsRegistry;

@AllArgsConstructor
public class CustomRowModeStrategy extends AbstractThingDataRepositoryStrategy {

    private final ThingsRegistry registry;
    //自定义存储帮助类
    private final CustomHelper helper;



    @Override
    public String getId() {
        return "custom-row";
    }

    @Override
    public String getName() {
        return "自定义-行式存储";
    }

    @Override
    public SaveOperations createOpsForSave(OperationsContext context) {
        //创建自定义SaveOperations类，返回自定义SaveOperations对象
        return new CustomRowModeSaveOperations(
            registry,
            context.getMetricBuilder(),
            context.getSettings(),
            helper);
    }

    @Override
    protected QueryOperations createForQuery(String thingType, String templateId, String thingId, OperationsContext context) {
        //创建自定义QueryOperations类，返回自定义QueryOperations对象
        return new CustomModeQueryOperations(
            thingType,
            templateId,
            thingId,
            context.getMetricBuilder(),
            context.getSettings(),
            registry,
            helper);
    }

    @Override
    protected DDLOperations createForDDL(String thingType, String templateId, String thingId, OperationsContext context) {
        //创建自定义DDLOperations类，返回自定义DDLOperations对象
        return new CustomRowModeDDLOperations(
            thingType,
            templateId,
            thingId,
            context.getMetricBuilder(),
            context.getSettings(),
            registry,
            helper);
    }

    @Override
    public int getOrder() {
        return 10000;
    }
}