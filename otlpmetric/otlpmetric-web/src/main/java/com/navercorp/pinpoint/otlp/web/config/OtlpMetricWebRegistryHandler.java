package com.navercorp.pinpoint.otlp.web.config;

import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.web.vo.handler.FieldAttributeHandler;
import com.navercorp.pinpoint.otlp.web.vo.*;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class OtlpMetricWebRegistryHandler implements MyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(OtlpMetricGroupsQueryParam.class);
        typeAliasRegistry.registerAlias(OtlpMetricNamesQueryParam.class);
        typeAliasRegistry.registerAlias(OtlpMetricDetailsQueryParam.class);
        typeAliasRegistry.registerAlias(FieldAttribute.class);
        typeAliasRegistry.registerAlias(OtlpMetricChartQueryParameter.class);
        typeAliasRegistry.registerAlias(OtlpMetricChartResult.class);
        typeAliasRegistry.registerAlias(AggreFunc.class);
        typeAliasRegistry.registerAlias(DataType.class);
        typeAliasRegistry.registerAlias(OtlpMetricChartSummary.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(FieldAttribute.class, FieldAttributeHandler.class);
    }
}
