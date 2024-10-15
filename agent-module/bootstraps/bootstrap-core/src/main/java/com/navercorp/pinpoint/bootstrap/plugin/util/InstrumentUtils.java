/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.util;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class InstrumentUtils {

    public static InstrumentMethod findMethod(InstrumentClass clazz, String methodName, String... parameterTypes) throws NotFoundInstrumentException {
        final InstrumentMethod method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (method == null) {
            throw new NotFoundInstrumentException("Cannot find method " + methodName + " with parameter types: " + Arrays.toString(parameterTypes));
        }
        return method;
    }

    public static InstrumentMethod findMethodOrIgnore(InstrumentClass clazz, String methodName, String... parameterTypes) throws NotFoundInstrumentException {
        final InstrumentMethod method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (method == null) {
            return new NullInstrumentMethod();
        }
        return method;
    }

    public static InstrumentMethod findConstructor(InstrumentClass clazz, String... parameterTypes) throws NotFoundInstrumentException {
        InstrumentMethod constructor = clazz.getConstructor(parameterTypes);
        if (constructor == null) {
            throw new NotFoundInstrumentException("Cannot find constructor with parameter types: " + Arrays.toString(parameterTypes));
        }
        return constructor;
    }

    public static InstrumentMethod findConstructorOrIgnore(InstrumentClass clazz, String... parameterTypes) throws NotFoundInstrumentException {
        InstrumentMethod constructor = clazz.getConstructor(parameterTypes);
        if (constructor == null) {
            return new NullInstrumentMethod();
        }
        return constructor;
    }

    static class NullInstrumentMethod implements InstrumentMethod {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String[] getParameterTypes() {
            return new String[0];
        }

        @Override
        public String getReturnType() {
            return "";
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public boolean isConstructor() {
            return false;
        }

        @Override
        public MethodDescriptor getDescriptor() {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException {
            return 0;
        }

        @Override
        public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
            return 0;
        }

        @Override
        public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
            return 0;
        }

        @Override
        public void addInterceptor(int interceptorId) throws InstrumentException {

        }

        @Override
        public Class<? extends Interceptor> loadInterceptorClass(String interceptorClassName) throws InstrumentException {
            throw new IllegalStateException("not implemented");
        }
    }
}
