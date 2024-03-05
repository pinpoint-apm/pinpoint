/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.configuration;

import com.navercorp.pinpoint.batch.alarm.dao.model.BatchQueryParameter;
import com.navercorp.pinpoint.batch.alarm.vo.AgentFieldUsage;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsage;
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsageCount;
import com.navercorp.pinpoint.common.dao.pinot.MultiValueTagTypeHandler;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.mybatis.typehandler.TagTypeHandler;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;

/**
 * @author minwoo-jung
 */
public class BatchRegistryHandler implements MyBatisRegistryHandler {

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(AgentUsage.class);
        typeAliasRegistry.registerAlias(AgentFieldUsage.class);
        typeAliasRegistry.registerAlias(AgentUsageCount.class);
        typeAliasRegistry.registerAlias(BatchQueryParameter.class);
        typeAliasRegistry.registerAlias(Tag.class);
        typeAliasRegistry.registerAlias(MultiValueTagTypeHandler.class);
        typeAliasRegistry.registerAlias(TagInformation.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(Tag.class, TagTypeHandler.class);
    }
}
