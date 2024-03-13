/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.config;

import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameter;
import com.navercorp.pinpoint.common.dao.pinot.MultiValueTagTypeHandler;
import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameterV2;
import com.navercorp.pinpoint.metric.common.config.CommonRegistryHandler;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.MinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.DoubleTypeHandler;
import com.navercorp.pinpoint.metric.common.mybatis.typehandler.TagTypeHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author minwoo.jung
 */
public class InspectorRegistryHandler implements MyBatisRegistryHandler {

    private final MyBatisRegistryHandler registryHandler;

    public InspectorRegistryHandler() {
        this.registryHandler = new CommonRegistryHandler();
    }

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        registryHandler.registerTypeAlias(typeAliasRegistry);

        typeAliasRegistry.registerAlias(SystemMetricPoint.class);
        typeAliasRegistry.registerAlias(AvgMinMaxMetricPoint.class);
        typeAliasRegistry.registerAlias(MinMaxMetricPoint.class);
        typeAliasRegistry.registerAlias(AvgMinMetricPoint.class);
        typeAliasRegistry.registerAlias(InspectorQueryParameter.class);
        typeAliasRegistry.registerAlias(InspectorQueryParameterV2.class);
        typeAliasRegistry.registerAlias("DoubleHandler", DoubleTypeHandler.class);
        typeAliasRegistry.registerAlias(TagInformation.class);
        typeAliasRegistry.registerAlias(MultiValueTagTypeHandler.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(Tag.class, TagTypeHandler.class);
    }
}
