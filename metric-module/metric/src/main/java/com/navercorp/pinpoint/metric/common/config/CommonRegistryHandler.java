package com.navercorp.pinpoint.metric.common.config;

import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.mybatis.MetricDataTypeHandler;
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
        typeAliasRegistry.registerAlias("Number", Number.class);
        
        typeAliasRegistry.registerAlias("Tag", Tag.class);
        typeAliasRegistry.registerAlias("DoubleMetric", DoubleMetric.class);
        typeAliasRegistry.registerAlias("MetricData", MetricData.class);
        typeAliasRegistry.registerAlias("MetricDataName", MetricDataName.class);
        typeAliasRegistry.registerAlias("MetricDataType", MetricDataType.class);
        typeAliasRegistry.registerAlias("MetricTag", MetricTag.class);
        typeAliasRegistry.registerAlias("MetricTagKey", MetricTagKey.class);
        typeAliasRegistry.registerAlias("MetricDataTypeHandler", MetricDataTypeHandler.class);
        typeAliasRegistry.registerAlias("TagListTypeHandler", com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler.class);
    }


    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(MetricDataType.class, MetricDataTypeHandler.class);
        typeHandlerRegistry.register(List.class, com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler.class);
    }

}
