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

package com.navercorp.pinpoint.mybatis;

import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;
import java.util.Objects;

public class MyBatisRegistryHandlerChain implements MyBatisRegistryHandler {
    private final List<? extends MyBatisRegistryHandler> handlers;

    public MyBatisRegistryHandlerChain(List<? extends MyBatisRegistryHandler> handlers) {
        this.handlers = Objects.requireNonNull(handlers, "handlers");
    }

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        for (MyBatisRegistryHandler handler : handlers) {
            handler.registerTypeAlias(typeAliasRegistry);
        }
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        for (MyBatisRegistryHandler handler : handlers) {
            handler.registerTypeHandler(typeHandlerRegistry);
        }
    }
}
