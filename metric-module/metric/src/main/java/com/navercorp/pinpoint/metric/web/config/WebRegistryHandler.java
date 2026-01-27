package com.navercorp.pinpoint.metric.web.config;

import com.navercorp.pinpoint.common.timeseries.point.DoubleDataPoint;
import com.navercorp.pinpoint.common.timeseries.point.LongDataPoint;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.metric.web.dao.model.HostInfoSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.MetricInfoSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.MetricTagsSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.TagListTypeHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;

public class WebRegistryHandler implements MyBatisRegistryHandler {

    public WebRegistryHandler() {
    }

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {

        typeAliasRegistry.registerAlias(Range.class);

        typeAliasRegistry.registerAlias(DoubleDataPoint.class);
        typeAliasRegistry.registerAlias(LongDataPoint.class);
        typeAliasRegistry.registerAlias(SystemMetricDataSearchKey.class);

        typeAliasRegistry.registerAlias("metricInfoSearchKey", MetricInfoSearchKey.class);
        typeAliasRegistry.registerAlias("metricTagsSearchKey", MetricTagsSearchKey.class);
        typeAliasRegistry.registerAlias("hostInfoSearchKey", HostInfoSearchKey.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

        typeHandlerRegistry.register(List.class, TagListTypeHandler.class);
    }
}
