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
 *
 */

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.dao.mysql.vo.AgentIndex;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ApplicationHasAgent;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ApplicationIndexDto;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ServiceIdAndApplicationName;
import com.navercorp.pinpoint.collector.dao.mysql.vo.ServiceIndexDto;
import com.navercorp.pinpoint.collector.vo.ApplicationIndex;
import com.navercorp.pinpoint.collector.vo.ServiceHasApplication;
import com.navercorp.pinpoint.collector.vo.ServiceIndex;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.mybatis.typehandler.UUIDByteArrayTypeHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.UUID;

public class CollectorCommonMyBatisRegistryHandler implements CollectorMyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(Range.class);

        typeAliasRegistry.registerAlias(AgentIndex.class);
        typeAliasRegistry.registerAlias(ApplicationHasAgent.class);
        typeAliasRegistry.registerAlias(ApplicationIndexDto.class);
        typeAliasRegistry.registerAlias(ApplicationIndex.class);
        typeAliasRegistry.registerAlias(ServiceHasApplication.class);
        typeAliasRegistry.registerAlias(ServiceIdAndApplicationName.class);
        typeAliasRegistry.registerAlias(ServiceIndexDto.class);
        typeAliasRegistry.registerAlias(ServiceIndex.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        typeHandlerRegistry.register(UUID.class, new UUIDByteArrayTypeHandler());
    }

}
