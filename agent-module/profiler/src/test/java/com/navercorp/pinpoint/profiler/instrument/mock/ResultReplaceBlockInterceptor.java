/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceBlockAroundInterceptor;

public class ResultReplaceBlockInterceptor implements ResultReplaceBlockAroundInterceptor {

    public static boolean before;
    public static boolean after;
    public static Object beforeTarget;
    public static Class<?> beforeReturnType;
    public static Object[] beforeArgs;
    public static Object afterTarget;
    public static Class<?> afterReturnType;
    public static Object[] afterArgs;
    public static Object result;
    public static Throwable throwable;

    /** what before() hands to the weaver; after() must receive exactly this instance back. */
    public static TraceBlock blockToReturn;
    public static TraceBlock afterBlock;

    public static boolean useReplacement;
    public static Object replacement;

    public static void clear() {
        before = false;
        after = false;
        beforeTarget = null;
        beforeReturnType = null;
        beforeArgs = null;
        afterTarget = null;
        afterReturnType = null;
        afterArgs = null;
        result = null;
        throwable = null;
        blockToReturn = null;
        afterBlock = null;
        useReplacement = false;
        replacement = null;
    }

    @Override
    public TraceBlock before(Object target, Class<?> returnType, Object[] args) {
        before = true;
        beforeTarget = target;
        beforeReturnType = returnType;
        beforeArgs = args;
        return blockToReturn;
    }

    @Override
    public Object after(TraceBlock block, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        after = true;
        afterBlock = block;
        afterTarget = target;
        afterReturnType = returnType;
        afterArgs = args;
        ResultReplaceBlockInterceptor.result = result;
        ResultReplaceBlockInterceptor.throwable = throwable;
        if (useReplacement) {
            return replacement;
        }
        return result;
    }
}
