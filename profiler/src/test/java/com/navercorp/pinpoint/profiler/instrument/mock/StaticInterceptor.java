/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.mock;

import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;

import java.util.Arrays;

/**
 * @author jaehong.kim
 */
public class StaticInterceptor implements StaticAroundInterceptor {
    public static boolean before;
    public static boolean after;
    public static Object beforeTarget;
    public static String beforeClassName;
    public static String beforeMethodName;
    public static String beforeParameterDescription;
    public static Object[] beforeArgs;
    public static Object afterTarget;
    public static String afterClassName;
    public static String afterMethodName;
    public static String afterParameterDescription;
    public static Object[] afterArgs;
    public static Object result;
    public static Throwable throwable;

    public static void clear() {
        before = false;
        after = false;
        beforeTarget = null;
        beforeClassName = null;
        beforeMethodName = null;
        beforeParameterDescription = null;
        beforeArgs = null;
        afterTarget = null;
        afterClassName = null;
        afterMethodName = null;
        afterParameterDescription = null;
        afterArgs = null;
        result = null;
        throwable = null;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        this.before = true;
        this.beforeTarget = target;
        this.beforeClassName = className;
        this.beforeMethodName = methodName;
        this.beforeParameterDescription = parameterDescription;
        this.beforeArgs = args;
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        this.after = true;
        this.afterTarget = target;
        this.afterClassName = className;
        this.afterMethodName = methodName;
        this.afterParameterDescription = parameterDescription;
        this.afterArgs = args;
        this.result = result;
        this.throwable = throwable;

    }

    private String toArgs(Object[] args) {
        if(args == null) {
            return "null";
        }

        return Arrays.asList(args).toString();
    }

}
