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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class InstrumentUtils {

    public static void addInterceptor(InstrumentClass clazz, String methodName, String[] parameterTypes, String interceptorClassName, String scopeName) throws InstrumentException {
        addInterceptor(clazz, methodName, parameterTypes, interceptorClassName, scopeName, null);
    }

    public static void addInterceptor(InstrumentClass clazz, String methodName, String[] parameterTypes, String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        if (clazz == null) {
            throw new NullPointerException("clazz must not be null");
        }
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptorClassName == null) {
            throw new NullPointerException("interceptorClassName must not be null");
        }
        if (scopeName == null) {
            throw new NullPointerException("scopeName must not be null");
        }

        final InstrumentMethod method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (method == null) {
            throw new NotFoundInstrumentException("Cannot find method " + methodName + " with parameter types: " + Arrays.toString(parameterTypes));
        }
        method.addScopedInterceptor(interceptorClassName, scopeName, executionPolicy);
    }

    public static  void addInterceptor(InstrumentClass clazz, String methodName, String[] parameterTypes, String interceptorClassName, Object[] interceptorParams, String scopeName) throws InstrumentException {
        addInterceptor(clazz, methodName, parameterTypes, interceptorClassName, interceptorParams, scopeName, null);
    }

    public static void addInterceptor(InstrumentClass clazz, String methodName, String[] parameterTypes, String interceptorClassName, Object[] interceptorParams, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        if (clazz == null) {
            throw new NullPointerException("clazz must not be null");
        }
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptorClassName == null) {
            throw new NullPointerException("interceptorClassName must not be null");
        }
        if (scopeName == null) {
            throw new NullPointerException("scopeName must not be null");
        }

        final InstrumentMethod method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (method == null) {
            throw new NotFoundInstrumentException("Cannot find method " + methodName + " with parameter types: " + Arrays.toString(parameterTypes));
        }
        method.addScopedInterceptor(interceptorClassName, interceptorParams, scopeName, executionPolicy);
    }
}
