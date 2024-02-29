package com.navercorp.pinpoint.metric.web.config;

import com.navercorp.pinpoint.metric.common.config.CommonRegistryHandler;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.dao.model.HostInfoSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.MetricInfoSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.MetricTagsSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.DoubleToLongTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.DoubleTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.LongTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.TagListTypeHandler;
import com.navercorp.pinpoint.metric.common.mybatis.typehandler.TagTypeHandler;
import com.navercorp.pinpoint.metric.web.util.MetricsQueryParameter;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;

public class WebRegistryHandler implements MyBatisRegistryHandler {
    private final MyBatisRegistryHandler registryHandler;

    public WebRegistryHandler() {
        this.registryHandler = new CommonRegistryHandler();
    }

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        registryHandler.registerTypeAlias(typeAliasRegistry);

        typeAliasRegistry.registerAlias(Range.class);
        typeAliasRegistry.registerAlias(MetricsQueryParameter.class);

        typeAliasRegistry.registerAlias("DoubleHandler", DoubleTypeHandler.class);
        typeAliasRegistry.registerAlias("LongHandler", LongTypeHandler.class);
        typeAliasRegistry.registerAlias("DoubleToLongHandler", DoubleToLongTypeHandler.class);
        typeAliasRegistry.registerAlias("TagHandler", TagTypeHandler.class);
        typeAliasRegistry.registerAlias("TagListHandler", TagListTypeHandler.class);

        typeAliasRegistry.registerAlias(SampledSystemMetric.class);
        typeAliasRegistry.registerAlias(SystemMetricPoint.class);
        typeAliasRegistry.registerAlias(SystemMetricDataSearchKey.class);

        typeAliasRegistry.registerAlias("metricInfoSearchKey", MetricInfoSearchKey.class);
        typeAliasRegistry.registerAlias("metricTagsSearchKey", MetricTagsSearchKey.class);
        typeAliasRegistry.registerAlias("hostInfoSearchKey", HostInfoSearchKey.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        registryHandler.registerTypeHandler(typeHandlerRegistry);

        typeHandlerRegistry.register(Number.class, DoubleTypeHandler.class);
        typeHandlerRegistry.register(Number.class, LongTypeHandler.class);
        typeHandlerRegistry.register(Number.class, DoubleToLongTypeHandler.class);
        typeHandlerRegistry.register(Tag.class, TagTypeHandler.class);
        typeHandlerRegistry.register(List.class, TagListTypeHandler.class);
    }
}
