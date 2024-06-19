/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.config;

import com.navercorp.pinpoint.exceptiontrace.web.entity.ClpConvertedEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.GroupedFieldNameEntity;
import com.navercorp.pinpoint.exceptiontrace.web.query.ClpQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.query.ExceptionTraceQueryParameter;
import com.navercorp.pinpoint.mybatis.MyBatisRegistryHandler;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author intr3p1d
 */
public class ExceptionTraceRegistryHandler implements MyBatisRegistryHandler {
    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(ExceptionMetaDataEntity.class);
        typeAliasRegistry.registerAlias(GroupedFieldNameEntity.class);
        typeAliasRegistry.registerAlias(ExceptionTraceSummaryEntity.class);
        typeAliasRegistry.registerAlias(ExceptionTraceValueViewEntity.class);
        typeAliasRegistry.registerAlias(ClpConvertedEntity.class);
        typeAliasRegistry.registerAlias(ExceptionTraceQueryParameter.class);
        typeAliasRegistry.registerAlias(ClpQueryParameter.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

    }
}
