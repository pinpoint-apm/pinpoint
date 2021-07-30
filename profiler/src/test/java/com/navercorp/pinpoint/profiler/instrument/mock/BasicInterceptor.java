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

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor5;

public class BasicInterceptor implements AroundInterceptor5 {
    public static boolean before;
    public static boolean after;
    public static Object beforeTarget;
    public static Object beforeArg0;
    public static Object beforeArg1;
    public static Object beforeArg2;
    public static Object beforeArg3;
    public static Object beforeArg4;
    public static Object afterTarget;
    public static Object afterArg0;
    public static Object afterArg1;
    public static Object afterArg2;
    public static Object afterArg3;
    public static Object afterArg4;
    public static Object result;
    public static Throwable throwable;

    public static void clear() {
        before = false;
        after = false;
        beforeTarget = null;
        beforeArg0 = null;
        beforeArg1 = null;
        beforeArg2 = null;
        beforeArg3 = null;
        beforeArg4 = null;

        afterTarget = null;
        afterArg0 = null;
        afterArg1 = null;
        afterArg2 = null;
        afterArg3 = null;
        afterArg4 = null;
        result = null;
        throwable = null;
    }

    @Override
    public void before(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
        before = true;
        beforeTarget = target;
        beforeArg0 = arg0;
        beforeArg1 = arg1;
        beforeArg2 = arg2;
        beforeArg3 = arg3;
        beforeArg4 = arg4;
    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object result, Throwable throwable) {
        after = true;
        afterTarget = target;
        afterArg0 = arg0;
        afterArg1 = arg1;
        afterArg2 = arg2;
        afterArg3 = arg3;
        afterArg4 = arg4;
        BasicInterceptor.result = result;
        BasicInterceptor.throwable = throwable;
    }
}