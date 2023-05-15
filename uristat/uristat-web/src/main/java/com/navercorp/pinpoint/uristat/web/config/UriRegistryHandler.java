package com.navercorp.pinpoint.uristat.web.config;

import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class UriRegistryHandler implements MyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias("UriStatChartValue", UriStatChartValue.class);
        typeAliasRegistry.registerAlias("UriStatSummary", UriStatSummary.class);
        typeAliasRegistry.registerAlias("UriStatSummaryQueryParameter", UriStatSummaryQueryParameter.class);
        typeAliasRegistry.registerAlias("UriStatChartQueryParameter", UriStatChartQueryParameter.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

    }
}
