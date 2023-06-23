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

import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameter;
import com.navercorp.pinpoint.inspector.web.dao.pinot.MultiValueTagTypeHandler;
import com.navercorp.pinpoint.inspector.web.model.TagInformation;
import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.metric.common.config.CommonRegistryHandler;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.DoubleTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.TagTypeHandler;
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

        typeAliasRegistry.registerAlias("SystemMetricPoint", SystemMetricPoint.class);
        typeAliasRegistry.registerAlias("inspectorQueryParameter", InspectorQueryParameter.class);
        typeAliasRegistry.registerAlias("DoubleHandler", DoubleTypeHandler.class);
        typeAliasRegistry.registerAlias("TagInformation", TagInformation.class);
        typeAliasRegistry.registerAlias("MultiValueTagTypeHandler", MultiValueTagTypeHandler.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(Tag.class, TagTypeHandler.class);
    }
}
