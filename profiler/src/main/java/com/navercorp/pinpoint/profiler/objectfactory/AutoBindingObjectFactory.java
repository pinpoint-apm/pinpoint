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
package com.navercorp.pinpoint.profiler.objectfactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory.ByConstructor;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory.ByStaticFactoryMethod;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class AutoBindingObjectFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InstrumentContext pluginContext;
    private final ClassLoader classLoader;
    private final List<ArgumentProvider> commonProviders;

    public AutoBindingObjectFactory(ProfilerConfig profilerConfig, TraceContext traceContext, InstrumentContext pluginContext, ClassLoader classLoader, ArgumentProvider... argumentProviders) {
        Objects.requireNonNull(profilerConfig, "profilerConfig");
        Objects.requireNonNull(traceContext, "traceContext");

        this.pluginContext = Objects.requireNonNull(pluginContext, "pluginContext");
        this.classLoader = classLoader;
        this.commonProviders = newArgumentProvider(profilerConfig, traceContext, pluginContext, argumentProviders);
    }

    private List<ArgumentProvider> newArgumentProvider(ProfilerConfig profilerConfig, TraceContext traceContext, InstrumentContext pluginContext, ArgumentProvider[] argumentProviders) {
        final List<ArgumentProvider> commonProviders = new ArrayList<>();
        for (ArgumentProvider argumentProvider : argumentProviders) {
            commonProviders.add(argumentProvider);
        }
        ProfilerPluginArgumentProvider profilerPluginArgumentProvider = new ProfilerPluginArgumentProvider(profilerConfig, traceContext, pluginContext);
        commonProviders.add(profilerPluginArgumentProvider);
        return commonProviders;
    }

    public Object createInstance(ObjectFactory objectFactory, ArgumentProvider... providers) {
        final Class<?> type = pluginContext.injectClass(classLoader, objectFactory.getClassName());
        final Object[] arguments = objectFactory.getArguments();
        final ArgumentsResolver argumentsResolver = getArgumentResolver(arguments, providers);
        
        if (objectFactory instanceof ByConstructor) {
            return byConstructor(type, argumentsResolver);
        } else if (objectFactory instanceof ByStaticFactoryMethod) {
            return byStaticFactoryMethod(type, (ByStaticFactoryMethod) objectFactory, argumentsResolver);
        }
        
        throw new IllegalArgumentException("Unknown objectFactory type: " + objectFactory);
    }

    public Object createInstance(Class<?> type, Object[] arguments, ArgumentProvider... providers) {
        final ArgumentsResolver argumentsResolver = getArgumentResolver(arguments, providers);
        return byConstructor(type, argumentsResolver);
    }
    
    private Object byConstructor(Class<?> type, ArgumentsResolver argumentsResolver) {
        final ConstructorResolver resolver = new ConstructorResolver(type, argumentsResolver);
        
        if (!resolver.resolve()) {
            throw new PinpointException("Cannot find suitable constructor for " + type.getName());
        }
        
        final Constructor<?> constructor = resolver.getResolvedConstructor();
        final Object[] resolvedArguments = resolver.getResolvedArguments();
        
        if (isDebug) {
            logger.debug("Create instance by constructor {}, with arguments {}", constructor, Arrays.toString(resolvedArguments));
        }
        
        try {
            return constructor.newInstance(resolvedArguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke constructor: " + constructor + ", arguments: " + Arrays.toString(resolvedArguments), e);
        }
    }

    private Object byStaticFactoryMethod(Class<?> type, ByStaticFactoryMethod staticFactoryMethod, ArgumentsResolver argumentsResolver) {
        StaticMethodResolver resolver = new StaticMethodResolver(type, staticFactoryMethod.getFactoryMethodName(), argumentsResolver);
        
        if (!resolver.resolve()) {
            throw new PinpointException("Cannot find suitable factory method " + type.getName() + "." + staticFactoryMethod.getFactoryMethodName());
        }
        
        final Method method = resolver.getResolvedMethod();
        final Object[] resolvedArguments = resolver.getResolvedArguments();

        if (isDebug) {
            logger.debug("Create instance by static factory method {}, with arguments {}", method, Arrays.toString(resolvedArguments));
        }

        try {
            return method.invoke(null, resolvedArguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke factory method: " + type.getName() + "." + staticFactoryMethod.getFactoryMethodName() + ", arguments: " + Arrays.toString(resolvedArguments), e);
        }

    }
    
    private ArgumentsResolver getArgumentResolver(Object[] argument, ArgumentProvider[] providers) {
        final List<ArgumentProvider> merged = new ArrayList<>(commonProviders);
        merged.addAll(Arrays.asList(providers));
        
        if (argument != null) {
            merged.add(new OrderedValueProvider(this, argument));
        }
        
        return new ArgumentsResolver(merged);
    }
}
