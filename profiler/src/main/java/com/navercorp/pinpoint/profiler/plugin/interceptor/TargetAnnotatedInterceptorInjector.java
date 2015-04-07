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
package com.navercorp.pinpoint.profiler.plugin.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.plugin.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.plugin.TargetFilter;
import com.navercorp.pinpoint.bootstrap.plugin.TargetMethod;
import com.navercorp.pinpoint.bootstrap.plugin.Targets;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.TypeUtils;
import com.navercorp.pinpoint.profiler.plugin.editor.ClassCookBook;
import com.navercorp.pinpoint.profiler.plugin.editor.ClassRecipe;
import com.navercorp.pinpoint.profiler.plugin.editor.ConstructorEditor;
import com.navercorp.pinpoint.profiler.plugin.editor.DedicatedMethodEditor;
import com.navercorp.pinpoint.profiler.plugin.editor.FilteringMethodEditor;
import com.navercorp.pinpoint.profiler.plugin.editor.MethodEditor;
import com.navercorp.pinpoint.profiler.plugin.editor.MethodRecipe;
import com.navercorp.pinpoint.profiler.plugin.objectfactory.AutoBindingObjectFactory;

/**
 * @author Jongho Moon
 *
 */

public class TargetAnnotatedInterceptorInjector implements ClassRecipe {
    private final DefaultProfilerPluginContext pluginContext;
    private final String interceptorName;
    private final Object[] providedArguments;


    public TargetAnnotatedInterceptorInjector(DefaultProfilerPluginContext pluginContext, String interceptorName, Object[] providedArguments) {
        this.pluginContext = pluginContext;
        this.interceptorName = interceptorName;
        this.providedArguments = providedArguments;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws Throwable {
        Class<? extends Interceptor> interceptorType = TypeUtils.loadClass(classLoader, interceptorName);
        
        AnnotatedInterceptorInjector injector = new AnnotatedInterceptorInjector(pluginContext, interceptorName, providedArguments, null, null);
        ClassRecipe editor = createMethodEditor(interceptorType, target,  injector);
        
        editor.edit(classLoader, target);
    }
    
    private ClassRecipe createMethodEditor(Class<?> interceptorType, InstrumentClass targetClass, InterceptorInjector injector) {
        List<MethodEditor> editors = new ArrayList<MethodEditor>();
        
        Targets targets = interceptorType.getAnnotation(Targets.class);
        
        if (targets != null) {
            for (TargetMethod m : targets.methods()) {
                editors.add(createDedicatedMethodEditor(m, injector));
            }
            
            for (TargetConstructor c : targets.constructors()) {
                editors.add(createConstructorEditor(c, injector));
            }
            
            for (TargetFilter f : targets.filters()) {
                editors.add(createFilteredMethodEditor(f, targetClass, injector));
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
            editors.add(createFilteredMethodEditor(targetFilter, targetClass, injector));
        }
        
        if (editors.isEmpty()) {
            throw new PinpointException("No target is specified. At least one of @Targets, @TargetMethod, @TargetConstructor, @TargetFilter must present. interceptor: " + interceptorName);
        }
        
        return editors.size() == 1 ? editors.get(0) : new ClassCookBook(editors);
    }
    
    private MethodEditor createDedicatedMethodEditor(TargetMethod annotation, InterceptorInjector injector) {
        String methodName = annotation.name();
        
        if (methodName == null) {
            throw new PinpointException("name() of @TargetMethod cannot be null: " + interceptorName);
        }
        
        String[] parameterTypeNames = annotation.paramTypes();
        
        return new DedicatedMethodEditor(methodName, parameterTypeNames, Arrays.<MethodRecipe>asList(injector), null, false);
    }
    
    private MethodEditor createConstructorEditor(TargetConstructor annotation, InterceptorInjector injector) {
        String[] parameterTypeNames = annotation.value();
        return new ConstructorEditor(parameterTypeNames, Arrays.<MethodRecipe>asList(injector), null, false);
    }
    
    private MethodEditor createFilteredMethodEditor(TargetFilter annotation, InstrumentClass targetClass, InterceptorInjector injector) {
        Class<? extends MethodFilter> type = annotation.value();
        
        if (type == null) {
            type = annotation.type();
        }
        
        if (type == null) {
            throw new PinpointException("value and type of @TargetFilter are null: " + interceptorName);
        }
        
        String[] constructorArguments = annotation.constructorArguments();
        
        AutoBindingObjectFactory<MethodFilter> filterFactory = new AutoBindingObjectFactory<MethodFilter>(pluginContext, targetClass, constructorArguments);
        MethodFilter filter = filterFactory.createInstance(type);
        
        return new FilteringMethodEditor(filter, Arrays.<MethodRecipe>asList(injector), null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TargetAnnotatedInterceptorInjector [interceptorClass=");
        builder.append(interceptorName);
        
        if (providedArguments != null) {
            builder.append(", constructorArguments=");
            builder.append(Arrays.toString(providedArguments));
        }
        
        builder.append(']');
        return builder.toString();
    }
}
