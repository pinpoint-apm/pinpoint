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
package com.navercorp.pinpoint.profiler.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Jongho Moon
 *
 */
public class ConstructorResolver<T> {
    private final List<ParameterResolver> parameterResolvers;
    private final Class<? extends T> type;
    
    private Constructor<? extends T> resolvedConstructor;
    private Object[] resolvedArguments;
    
    public ConstructorResolver(Class<? extends T> type, List<ParameterResolver> resolvers) {
        this.type = type;
        this.parameterResolvers = resolvers;
    }

    @SuppressWarnings("unchecked")
    public boolean resolve() {
        Constructor<? extends T>[] constructors = (Constructor<? extends T>[]) type.getConstructors();
        Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        
        for (Constructor<? extends T> constructor : constructors) {
            Object[] resolvedArguments = tryConstructor(constructor);
            
            if (resolvedArguments != null) {
                this.resolvedConstructor = constructor;
                this.resolvedArguments = resolvedArguments;

                return true;
            }
        }
        
        return false;
    }

    private Object[] tryConstructor(Constructor<?> constructor) {
        Class<?>[] types = constructor.getParameterTypes();
        Annotation[][] annotations = constructor.getParameterAnnotations();

        int length = types.length;
        Object[] arguments = new Object[length];
        
        for (ParameterResolver resolver : parameterResolvers) {
            if (resolver instanceof JudgingParameterResolver) {
                ((JudgingParameterResolver)resolver).prepare();
            }
        }
        
        outer:
        for (int i = 0; i < length; i++) {
            for (ParameterResolver resolver : parameterResolvers) {
                Option<Object> resolved = resolver.resolve(i, types[i], annotations[i]);
                
                if (resolved.hasValue()) {
                    arguments[i] = resolved.getValue();
                    continue outer;
                }
            }
            
            return null;
        }
        
        for (ParameterResolver resolver : parameterResolvers) {
            if (resolver instanceof JudgingParameterResolver) {
                if (!((JudgingParameterResolver)resolver).isAcceptable()) {
                    return null;
                }
            }
        }
        
        return arguments;
    }
    
    public Constructor<? extends T> getResolvedConstructor() {
        return resolvedConstructor;
    }

    public Object[] getResolvedArguments() {
        return resolvedArguments;
    }

    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>() {

        @Override
        public int compare(Constructor<?> o1, Constructor<?> o2) {
            int p1 = o1.getParameterTypes().length;
            int p2 = o2.getParameterTypes().length;
            
            return (p1 < p2) ? 1 : ((p1 == p2) ? 0 : -1);
        }
        
    };
}
