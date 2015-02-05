/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.editor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.Option;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.TypeUtils;
import com.navercorp.pinpoint.exception.PinpointException;

public class DefaultInterceptorFactory implements InterceptorFactory {
    private static final Object[] NO_ARGS = new Object[0];

    private final ProfilerPluginContext pluginContext;
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final String interceptorClassName;
    private final Object[] providedArguments;
    
    public DefaultInterceptorFactory(ProfilerPluginContext pluginContext, ByteCodeInstrumentor instrumentor, TraceContext traceContext, String interceptorClassName, Object[] providedArguments) {
        this.pluginContext = pluginContext;
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
        this.interceptorClassName = interceptorClassName;
        this.providedArguments = providedArguments == null ? NO_ARGS : providedArguments;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, InstrumentClass target, MethodInfo targetMethod) {
        Class<?> interceptorClass;
        
        try {
            interceptorClass = classLoader.loadClass(interceptorClassName);
        } catch (ClassNotFoundException e) {
            throw new PinpointException("Cannot load interceptor class: " + interceptorClassName, e);
        }
        
        Constructor<?>[] constructors = interceptorClass.getConstructors();
        Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        
        for (Constructor<?> constructor : constructors) {
            ConstructorResolver resolver = new ConstructorResolver(target, targetMethod, constructor);
            Object[] resolvedArguments = resolver.resolve();
            
            if (resolvedArguments != null) {
                return (Interceptor)invokeConstructor(constructor, resolvedArguments);
            }
        }
        
        throw new PinpointException("Cannot find suitable constructor for " + interceptorClass.getName());
    }
    
    private Object invokeConstructor(Constructor<?> constructor, Object[] arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke constructor: " + constructor + ", arguments: " + Arrays.toString(arguments), e);
        }
    }
    
    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>() {

        @Override
        public int compare(Constructor<?> o1, Constructor<?> o2) {
            int p1 = o1.getParameterTypes().length;
            int p2 = o2.getParameterTypes().length;
            
            return (p1 < p2) ? 1 : ((p1 == p2) ? 0 : -1);
        }
        
    };
    
    private class ConstructorResolver {
        private final InstrumentClass target;
        private final MethodInfo targetMethod;
        
        private final Constructor<?> constructor;
        
        private int argumentIndex = 0;
        
        
        public ConstructorResolver(InstrumentClass target, MethodInfo targetMethod, Constructor<?> constructor) {
            this.target = target;
            this.targetMethod = targetMethod;
            this.constructor = constructor;
        }

        public Object[] resolve() {
            Class<?>[] types = constructor.getParameterTypes();
            Annotation[][] annotations = constructor.getParameterAnnotations();

            int length = types.length;
            Object[] arguments = new Object[length];
            
            for (int i = 0; i < length; i++) {
                Option<Object> resolved = resolveArgument(types[i], annotations[i]);
                
                if (!resolved.hasValue()) {
                    return null;
                }
                
                arguments[i] = resolved.getValue();
            }
            
            if (argumentIndex != providedArguments.length) {
                return null;
            }
            
            return arguments;
        }
        
        private Option<Object> resolveArgument(Class<?> type, Annotation[] annotations) {
            Object result = resolvePinpointObject(type, annotations);
            
            if (result != null) {
                return Option.withValue(result);
            }
            
            if (providedArguments.length >= argumentIndex + 1) {
                Object candidate = providedArguments[argumentIndex];
                argumentIndex++;
                
                if (type.isPrimitive()) {
                    if (candidate == null) {
                        return Option.<Object>empty();
                    }
                    
                    if (TypeUtils.getWrapperOf(type) == candidate.getClass()) {
                        return Option.withValue(candidate); 
                    }
                } else {
                    if (type.isAssignableFrom(candidate.getClass())) {
                        return Option.withValue(candidate);
                    }
                }
            }
            
            return Option.<Object>empty();
        }
        
        private Object resolvePinpointObject(Class<?> type, Annotation[] annotations) {
            if (type == TraceContext.class) {
                return traceContext;
            } else if (type == MethodDescriptor.class) {
                return targetMethod.getDescriptor();
            } else if (type == ByteCodeInstrumentor.class) {
                return instrumentor;
            } else if (type == MethodInfo.class) {
                return targetMethod;
            } else if (type == InstrumentClass.class) {
                return target;
            }
            
            return null;
        }
    }
}
