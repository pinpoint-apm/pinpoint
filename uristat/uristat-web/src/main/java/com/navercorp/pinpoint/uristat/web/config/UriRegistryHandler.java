package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.uristat.web.entity.ApdexChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.ChartCommonEntity;
import com.navercorp.pinpoint.uristat.web.entity.FailureChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.LatencyChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.TotalChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class UriRegistryHandler implements MyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(UriStatChartValue.class);
        typeAliasRegistry.registerAlias(UriStatSummary.class);
        typeAliasRegistry.registerAlias(ApdexChartEntity.class);
        typeAliasRegistry.registerAlias(ChartCommonEntity.class);
        typeAliasRegistry.registerAlias(FailureChartEntity.class);
        typeAliasRegistry.registerAlias(LatencyChartEntity.class);
        typeAliasRegistry.registerAlias(TotalChartEntity.class);
        typeAliasRegistry.registerAlias(UriStatSummaryEntity.class);
        typeAliasRegistry.registerAlias(UriStatSummaryQueryParameter.class);
        typeAliasRegistry.registerAlias(UriStatChartQueryParameter.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

    }
}
