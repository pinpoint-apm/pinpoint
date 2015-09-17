/**
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

import java.lang.reflect.Modifier;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorInvokerHelper;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author Jongho Moon
 *
 */
public class InvokeCodeGenerator {
    protected final Class<?> interceptorClass;
    protected final InstrumentMethod targetMethod;
    protected final int interceptorId;
    protected final Type type;
    
    public InvokeCodeGenerator(int interceptorId, Class<?> interceptorClass, InstrumentMethod targetMethod) {
        this.interceptorClass = interceptorClass;
        this.targetMethod = targetMethod;
        this.interceptorId = interceptorId;
        
        if (SimpleAroundInterceptor.class.isAssignableFrom(interceptorClass)) {
            type = Type.SIMPLE;
        } else if (StaticAroundInterceptor.class.isAssignableFrom(interceptorClass)) {
            type = Type.STATIC;
        } else {
            type = Type.CUSTOM;
        }
    }

    protected enum Type {
        SIMPLE, STATIC, CUSTOM
    }

    protected String getInterceptorType() {
        switch (type) {
        case SIMPLE:
            return SimpleAroundInterceptor.class.getName();
        case STATIC:
            return StaticAroundInterceptor.class.getName();
        case CUSTOM:
            return interceptorClass.getName();
        }

        return null;
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
    
    protected String getInterceptorInvokerHelperClassName() {
        return InterceptorInvokerHelper.class.getName();
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