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
package com.navercorp.pinpoint.profiler.plugin.xml.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructors;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethods;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.ClassCookBook;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.ClassRecipe;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.ConstructorTransformer;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.DedicatedMethodTransformer;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.FilteringMethodTransformer;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.MethodRecipe;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.MethodTransformer;

/**
 * @author Jongho Moon
 *
 */

public class TargetAnnotatedInterceptorInjector implements ClassRecipe {
    private final TraceContext traceContext;
    private final InstrumentContext pluginContext;
    private final String interceptorClassName;
    private final Object[] providedArguments;
    
    private final String scopeName;
    private final ExecutionPolicy executionPoint;
    private final ProfilerConfig profilerConfig;
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;
    private final ApiMetaDataService apiMetaDataService;


    public TargetAnnotatedInterceptorInjector(ProfilerConfig profilerConfig, TraceContext traceContext, DataSourceMonitorRegistry dataSourceMonitorRegistry, ApiMetaDataService apiMetaDataService, InstrumentContext pluginContext,
                                              String interceptorClassName, Object[] providedArguments, String scopeName, ExecutionPolicy executionPoint) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (dataSourceMonitorRegistry == null) {
            throw new NullPointerException("dataSourceMonitorRegistry must not be null");
        }
        if (apiMetaDataService == null) {
            throw new NullPointerException("apiMetaDataService must not be null");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.traceContext = traceContext;
        this.dataSourceMonitorRegistry = dataSourceMonitorRegistry;
        this.apiMetaDataService = apiMetaDataService;
        this.pluginContext = pluginContext;
        this.interceptorClassName = interceptorClassName;
        this.providedArguments = providedArguments;
        this.scopeName = scopeName;
        this.executionPoint = executionPoint;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws Throwable {
        Class<? extends Interceptor> interceptorType = pluginContext.injectClass(classLoader, interceptorClassName);
        
        AnnotatedInterceptorInjector injector = new AnnotatedInterceptorInjector(pluginContext, interceptorClassName, providedArguments, scopeName, executionPoint);
        ClassRecipe recipe = createMethodEditor(classLoader, interceptorType, target,  injector);
        
        recipe.edit(classLoader, target);
    }
    
    private ClassRecipe createMethodEditor(ClassLoader classLoader, Class<?> interceptorType, InstrumentClass targetClass, AnnotatedInterceptorInjector injector) {
        List<MethodTransformer> editors = new ArrayList<MethodTransformer>();
        
        TargetMethods targetMethods = interceptorType.getAnnotation(TargetMethods.class);
        if (targetMethods != null) {
            for (TargetMethod m : targetMethods.value()) {
                editors.add(createDedicatedMethodEditor(m, injector));
            }
        }

        TargetConstructors targetConstructors = interceptorType.getAnnotation(TargetConstructors.class);
        if (targetConstructors != null) {
            for (TargetConstructor c : targetConstructors.value()) {
                editors.add(createConstructorEditor(c, injector));
            }
        }

        TargetMethod targetMethod = interceptorType.getAnnotation(TargetMethod.class);
        if (targetMethod != null) {
            editors.add(createDedicatedMethodEditor(targetMethod, injector));
        }
        
        TargetConstructor targetConstructor = interceptorType.getAnnotation(TargetConstructor.class);
        if (targetConstructor != null) {
            editors.add(createConstructorEditor(targetConstructor, injector));
        }
        
        TargetFilter targetFilter = interceptorType.getAnnotation(TargetFilter.class);
        if (targetFilter != null) {
            editors.add(createFilteredMethodEditor(targetFilter, targetClass, injector, classLoader));
        }
        
        if (editors.isEmpty()) {
            throw new PinpointException("No target is specified. At least one of @Targets, @TargetMethod, @TargetConstructor, @TargetFilter must present. interceptor: " + interceptorClassName);
        }
        
        return editors.size() == 1 ? editors.get(0) : new ClassCookBook(editors);
    }
    
    private MethodTransformer createDedicatedMethodEditor(TargetMethod annotation, AnnotatedInterceptorInjector injector) {
        String methodName = annotation.name();
        
        if (methodName == null) {
            throw new PinpointException("name() of @TargetMethod cannot be null: " + interceptorClassName);
        }
        
        String[] parameterTypeNames = annotation.paramTypes();
        
        return new DedicatedMethodTransformer(methodName, parameterTypeNames, Arrays.<MethodRecipe>asList(injector), null, false);
    }
    
    private MethodTransformer createConstructorEditor(TargetConstructor annotation, AnnotatedInterceptorInjector injector) {
        String[] parameterTypeNames = annotation.value();
        return new ConstructorTransformer(parameterTypeNames, Arrays.<MethodRecipe>asList(injector), null, false);
    }
    
    private MethodTransformer createFilteredMethodEditor(TargetFilter annotation, InstrumentClass targetClass, AnnotatedInterceptorInjector injector, ClassLoader classLoader) {
        String type = annotation.type();
        
        if (type == null) {
            throw new PinpointException("type of @TargetFilter is null: " + interceptorClassName);
        }

        final InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(dataSourceMonitorRegistry, apiMetaDataService, targetClass);
        AutoBindingObjectFactory filterFactory = new AutoBindingObjectFactory(profilerConfig, traceContext, pluginContext, classLoader, interceptorArgumentProvider);
        MethodFilter filter = (MethodFilter)filterFactory.createInstance(ObjectFactory.byConstructor(type, (Object[]) annotation.constructorArguments()));
        MethodRecipe recipe = annotation.singleton() ? new SharedAnnotatedInterceptorInjector(injector) : injector;
        
        return new FilteringMethodTransformer(new MethodFilter[] { filter }, Arrays.<MethodRecipe>asList(recipe), null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TargetAnnotatedInterceptorInjector [interceptorClass=");
        builder.append(interceptorClassName);
        
        if (providedArguments != null) {
            builder.append(", constructorArguments=");
            builder.append(Arrays.toString(providedArguments));
        }
        
        if (scopeName != null) {
            builder.append(", scope=");
            builder.append(scopeName);
            builder.append(", executionPolicy=");
            builder.append(executionPoint);
        }
        
        builder.append(']');
        return builder.toString();
    }
}
