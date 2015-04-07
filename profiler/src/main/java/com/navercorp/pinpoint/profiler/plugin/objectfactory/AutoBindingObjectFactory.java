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
package com.navercorp.pinpoint.profiler.plugin.objectfactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class AutoBindingObjectFactory<T> {

    private final ProfilerPluginContext pluginContext;
    private final InterceptorGroup interceptorGroup;
    private final InstrumentClass targetClass;
    private final MethodInfo targetMethod;
    private final Object[] providedValues;
    
    public AutoBindingObjectFactory(ProfilerPluginContext pluginContext, InstrumentClass targetClass, Object[] providedValues) {
        this(pluginContext, null, targetClass, null, providedValues);
    }

    public AutoBindingObjectFactory(ProfilerPluginContext pluginContext, InterceptorGroup interceptorGroup, InstrumentClass targetClass, MethodInfo targetMethod, Object[] providedValues) {
        this.pluginContext = pluginContext;
        this.interceptorGroup = interceptorGroup;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.providedValues = providedValues;
    }
    
    public T createInstance(Class<? extends T> type) {
        ConstructorResolver<T> resolver = createConstructorResolver(targetClass, targetMethod, type);
        
        if (!resolver.resolve()) {
            throw new PinpointException("Cannot find suitable constructor for " + type.getName());
        }
        
        Constructor<? extends T> constructor = resolver.getResolvedConstructor();
        Object[] resolvedArguments = resolver.getResolvedArguments();
        
        return invokeConstructor(constructor, resolvedArguments);
    }

    private ConstructorResolver<T> createConstructorResolver(InstrumentClass target, MethodInfo targetMethod, Class<? extends T> interceptorClass) {
        List<ParameterResolver> suppliers = new ArrayList<ParameterResolver>();
        
        PinpointTypeResolver pinpointResolver = new PinpointTypeResolver(pluginContext, interceptorGroup, target, targetMethod);
        suppliers.add(pinpointResolver);
        
        if (providedValues != null) { 
            suppliers.add(new ProvidedValuesResolver(providedValues));
        }
        
        return new ConstructorResolver<T>(interceptorClass, suppliers);
    }

    private T invokeConstructor(Constructor<? extends T> constructor, Object[] arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke constructor: " + constructor + ", arguments: " + Arrays.toString(arguments), e);
        }
    }    
}
