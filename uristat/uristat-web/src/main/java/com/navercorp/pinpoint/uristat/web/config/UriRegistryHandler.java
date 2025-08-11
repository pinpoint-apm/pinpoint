package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.uristat.web.entity.UriApdexChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriHistogramTotalEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriHistogramFailEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriLatencyChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class UriRegistryHandler implements MyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(UriHistogramTotalEntity.class);
        typeAliasRegistry.registerAlias(UriHistogramFailEntity.class);

        typeAliasRegistry.registerAlias(UriApdexChartEntity.class);
        typeAliasRegistry.registerAlias(UriLatencyChartEntity.class);

        typeAliasRegistry.registerAlias(UriStatSummaryEntity.class);
        typeAliasRegistry.registerAlias(UriStatSummaryQueryParameter.class);
        typeAliasRegistry.registerAlias(UriStatChartQueryParameter.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

    }
}
