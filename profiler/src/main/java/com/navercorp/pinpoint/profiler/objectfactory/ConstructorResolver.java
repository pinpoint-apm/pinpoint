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
package com.navercorp.pinpoint.profiler.objectfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Jongho Moon
 *
 */
public class ConstructorResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<?> type;
    private final ArgumentsResolver argumentsResolver;
    
    private Constructor<?> resolvedConstructor;
    private Object[] resolvedArguments;

    public ConstructorResolver(Class<?> type, ArgumentsResolver argumentsResolver) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        this.type = type;
        this.argumentsResolver = argumentsResolver;
    }

    public boolean resolve() {
        final Constructor<?>[] constructors = type.getConstructors();
        Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        
        for (Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

            Object[] resolvedArguments = argumentsResolver.resolve(parameterTypes, parameterAnnotations);
            
            if (resolvedArguments != null) {
                this.resolvedConstructor = constructor;
                this.resolvedArguments = resolvedArguments;

                return true;
            }
        }
        if (logger.isWarnEnabled()) {
            resolveFailLog(type);
        }
        return false;
    }

    public Constructor<?> getResolvedConstructor() {
        return resolvedConstructor;
    }

    public Object[] getResolvedArguments() {
        return resolvedArguments;
    }

    private void resolveFailLog(Class<?> type) {
        final Constructor<?>[] constructors = type.getConstructors();

        for (Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            logger.warn("Constructor resolve fail. class:{} {}", type.getName(), Arrays.toString(parameterTypes));
            for (int i = 0; i < parameterTypes.length; i++) {
                final Class<?> parameterClass = parameterTypes[i];
                final ClassLoader parameterClassLoader = type.getClassLoader();
                logger.warn("index:{} {} cl:{}", i, parameterClass, parameterClassLoader);
            }
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
}
