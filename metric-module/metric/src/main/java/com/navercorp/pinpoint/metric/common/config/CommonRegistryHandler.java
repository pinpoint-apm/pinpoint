package com.navercorp.pinpoint.metric.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.mybatis.MetricDataTypeHandler;
import com.navercorp.pinpoint.metric.common.mybatis.typehandler.TagListSerializer;
import com.navercorp.pinpoint.metric.common.mybatis.typehandler.TagListTypeHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.TypeReference;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CommonRegistryHandler implements MyBatisRegistryHandler {

    private final ObjectMapper objectMapper;

    public CommonRegistryHandler(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
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
        typeAliasRegistry.registerAlias(TagListTypeHandler.class);
    }


    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(MetricDataType.class, MetricDataTypeHandler.class);

        TypeReference<List<Tag>> tagList = new TypeReference<>() {};
        TagListSerializer serializer = new TagListSerializer(objectMapper);
        typeHandlerRegistry.register(tagList, new TagListTypeHandler(serializer));
    }

}
