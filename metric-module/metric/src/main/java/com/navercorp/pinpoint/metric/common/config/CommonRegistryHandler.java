package com.navercorp.pinpoint.metric.common.config;

import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.mybatis.MetricDataTypeHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CommonRegistryHandler implements MyBatisRegistryHandler {

    public CommonRegistryHandler() {
    }

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(Number.class);
        
        typeAliasRegistry.registerAlias(Tag.class);
        typeAliasRegistry.registerAlias(DoubleMetric.class);
        typeAliasRegistry.registerAlias(MetricData.class);
        typeAliasRegistry.registerAlias(MetricDataName.class);
        typeAliasRegistry.registerAlias(MetricDataType.class);
        typeAliasRegistry.registerAlias(MetricTag.class);
        typeAliasRegistry.registerAlias(MetricTagKey.class);
        typeAliasRegistry.registerAlias(MetricDataTypeHandler.class);
        typeAliasRegistry.registerAlias(com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler.class);
    }


    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(MetricDataType.class, MetricDataTypeHandler.class);
        typeHandlerRegistry.register(List.class, com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler.class);
    }

}
