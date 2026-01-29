/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.web.config.pinot;

import com.navercorp.pinpoint.common.timeseries.point.DoubleDataPoint;
import com.navercorp.pinpoint.common.timeseries.point.LongDataPoint;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.web.definition.property.MetricDescriptor;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartQueryParameter;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartSummary;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricDataQueryParameter;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricDetailsQueryParam;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricGroupsQueryParam;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricNamesQueryParam;
import com.navercorp.pinpoint.otlp.web.vo.handler.FieldAttributeHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class OtlpMetricPinotRegistryHandler implements MyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(OtlpMetricGroupsQueryParam.class);
        typeAliasRegistry.registerAlias(OtlpMetricNamesQueryParam.class);
        typeAliasRegistry.registerAlias(OtlpMetricDetailsQueryParam.class);
        typeAliasRegistry.registerAlias(FieldAttribute.class);
        typeAliasRegistry.registerAlias(OtlpMetricChartQueryParameter.class);
        typeAliasRegistry.registerAlias(OtlpMetricDataQueryParameter.class);
        typeAliasRegistry.registerAlias(OtlpMetricChartResult.class);
        typeAliasRegistry.registerAlias(AggreFunc.class);
        typeAliasRegistry.registerAlias(DataType.class);
        typeAliasRegistry.registerAlias(OtlpMetricChartSummary.class);
        typeAliasRegistry.registerAlias(MetricDescriptor.class);

        typeAliasRegistry.registerAlias(LongDataPoint.class);
        typeAliasRegistry.registerAlias(DoubleDataPoint.class);

        typeAliasRegistry.registerAlias(Number.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(FieldAttribute.class, FieldAttributeHandler.class);
    }
}
