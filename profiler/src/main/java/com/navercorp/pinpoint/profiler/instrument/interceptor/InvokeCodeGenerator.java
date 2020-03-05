/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.lang.reflect.Modifier;

/**
 * @author Jongho Moon
 *
 */
public class InvokeCodeGenerator {
    protected final ApiMetaDataService apiMetaDataService;
    protected final InterceptorDefinition interceptorDefinition;
    protected final InstrumentMethod targetMethod;
    protected final int interceptorId;

    public InvokeCodeGenerator(int interceptorId, InterceptorDefinition interceptorDefinition, InstrumentMethod targetMethod, ApiMetaDataService apiMetaDataService) {
        if (interceptorDefinition == null) {
            throw new NullPointerException("interceptorDefinition");
        }
        if (targetMethod == null) {
            throw new NullPointerException("targetMethod");
        }
        if (apiMetaDataService == null) {
            throw new NullPointerException("apiMetaDataService");
        }

        this.interceptorDefinition = interceptorDefinition;
        this.targetMethod = targetMethod;
        this.interceptorId = interceptorId;
        this.apiMetaDataService = apiMetaDataService;

    }

    protected String getInterceptorType() {
//        return interceptorDefinition.getInterceptorClass().getName();
        return interceptorDefinition.getInterceptorBaseClass().getName();
    }

    protected String getParameterTypes() {
        String[] parameterTypes = targetMethod.getParameterTypes();
        return JavaAssistUtils.getParameterDescription(parameterTypes);
    }

    protected String getTarget() {
        return Modifier.isStatic(targetMethod.getModifiers()) ? "null" : "this";
    }

    protected String getArguments() {
        if (targetMethod.getParameterTypes().length == 0) {
            return "null";
        }

        return "$args";
    }
    
    protected int getApiId() {
        final MethodDescriptor descriptor = targetMethod.getDescriptor();
        final int apiId = apiMetaDataService.cacheApi(descriptor);
        return apiId;
    }


    protected String getInterceptorRegistryClassName() {
        return InterceptorRegistry.class.getName();
    }
    
    protected String getInterceptorVar() {
        return getInterceptorVar(interceptorId);
    }
    
    public static String getInterceptorVar(int interceptorId) {
        return "_$PINPOINT$_interceptor" + interceptorId;
    }
}