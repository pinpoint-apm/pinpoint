/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument;

import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;


/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultEngineComponent implements EngineComponent {

    private final ObjectBinderFactory objectBinderFactory;
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final InterceptorDefinitionFactory interceptorDefinitionFactory;
    private final Provider<ApiMetaDataService> apiMetaDataServiceProvider;
    private final ScopeFactory scopeFactory;

    public DefaultEngineComponent(ObjectBinderFactory objectBinderFactory,
                                  InterceptorRegistryBinder interceptorRegistryBinder,
                                  InterceptorDefinitionFactory interceptorDefinitionFactory,
                                  Provider<ApiMetaDataService> apiMetaDataServiceProvider,
                                  ScopeFactory scopeFactory) {
        this.objectBinderFactory = Assert.requireNonNull(objectBinderFactory, "objectBinderFactory");
        this.interceptorRegistryBinder = Assert.requireNonNull(interceptorRegistryBinder, "interceptorRegistryBinder");
        this.interceptorDefinitionFactory = Assert.requireNonNull(interceptorDefinitionFactory, "interceptorDefinitionFactory");
        this.apiMetaDataServiceProvider = Assert.requireNonNull(apiMetaDataServiceProvider, "apiMetaDataService");
        this.scopeFactory = Assert.requireNonNull(scopeFactory, "scopeFactory");
    }

    @Override
    public ScopeFactory getScopeFactory() {
        return scopeFactory;
    }

    @Override
    public InterceptorDefinition createInterceptorDefinition(Class<?> interceptorClazz) {
        return this.interceptorDefinitionFactory.createInterceptorDefinition(interceptorClazz);
    }

    @Override
    public ObjectBinderFactory getObjectBinderFactory() {
        return objectBinderFactory;
    }


    @Override
    public int addInterceptor(Interceptor interceptor) {
        return  interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(interceptor);
    }

    @Override
    public int cacheApi(MethodDescriptor methodDescriptor) {
        ApiMetaDataService apiMetaDataService = this.apiMetaDataServiceProvider.get();
        return apiMetaDataService.cacheApi(methodDescriptor);
    }

}

