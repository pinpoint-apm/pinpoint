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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe.ByConstructor;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe.ByStaticFactoryMethod;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;

/**
 * @author Jongho Moon
 *
 */
public class AutoBindingObjectFactory {
    private final DefaultProfilerPluginContext pluginContext;
    private final ClassLoader classLoader;
    private final List<ArgumentProvider> commonProviders;
    
    public AutoBindingObjectFactory(DefaultProfilerPluginContext pluginContext, ClassLoader classLoader, ArgumentProvider... argumentProviders) {
        this.pluginContext = pluginContext;
        this.classLoader = classLoader;
        this.commonProviders = new ArrayList<ArgumentProvider>(Arrays.asList(argumentProviders));
        this.commonProviders.add(new ProfilerPluginArgumentProvider(pluginContext));
    }
    
    public Object createInstance(ObjectRecipe recipe, ArgumentProvider... providers) {
        Class<?> type = pluginContext.getClassInjector().loadClass(classLoader, recipe.getClassName());
        ArgumentsResolver argumentsResolver = getArgumentResolver(recipe, providers);
        
        if (recipe instanceof ByConstructor) {
            return byConstructor(type, (ByConstructor)recipe, argumentsResolver);
        } else if (recipe instanceof ByStaticFactoryMethod) {
            return byStaticFactoryMethod(type, (ByStaticFactoryMethod)recipe, argumentsResolver);
        }
        
        throw new IllegalArgumentException("Unknown recipe type: " + recipe);
    }
    
    private Object byConstructor(Class<?> type, ByConstructor recipe, ArgumentsResolver argumentsResolver) {
        ConstructorResolver resolver = new ConstructorResolver(type, argumentsResolver);
        
        if (!resolver.resolve()) {
            throw new PinpointException("Cannot find suitable constructor for " + type.getName());
        }
        
        Constructor<?> constructor = resolver.getResolvedConstructor();
        Object[] resolvedArguments = resolver.getResolvedArguments();
        
        try {
            return constructor.newInstance(resolvedArguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke constructor: " + constructor + ", arguments: " + Arrays.toString(resolvedArguments), e);
        }
    }
    
    private Object byStaticFactoryMethod(Class<?> type, ByStaticFactoryMethod recipe, ArgumentsResolver argumentsResolver) {
        StaticMethodResolver resolver = new StaticMethodResolver(type, recipe.getFactoryMethodName(), argumentsResolver);
        
        if (!resolver.resolve()) {
            throw new PinpointException("Cannot find suitable factory method " + type.getName() + "." + recipe.getFactoryMethodName());
        }
        
        Method method = resolver.getResolvedMethod();
        Object[] resolvedArguments = resolver.getResolvedArguments();
        
        try {
            return method.invoke(null, resolvedArguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke factory method: " + type.getName() + "." + recipe.getFactoryMethodName() + ", arguments: " + Arrays.toString(resolvedArguments), e);
        }

    }
    
    private ArgumentsResolver getArgumentResolver(ObjectRecipe recipe, ArgumentProvider[] providers) {
        List<ArgumentProvider> merged = new ArrayList<ArgumentProvider>(commonProviders);
        merged.addAll(Arrays.asList(providers));
        
        if (recipe.getArguments() != null) { 
            merged.add(new OrderedValueProvider(this, recipe.getArguments()));
        }
        
        return new ArgumentsResolver(merged);
    }
}
