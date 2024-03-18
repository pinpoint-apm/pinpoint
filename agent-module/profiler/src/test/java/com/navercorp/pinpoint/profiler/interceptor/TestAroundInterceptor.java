/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;

/**
 * @author emeroad
 */
public class TestAroundInterceptor implements StaticAroundInterceptor {

    public TestBeforeInterceptor before = new TestBeforeInterceptor();
    public TestAfterInterceptor after = new TestAfterInterceptor();

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        before.before(target, className, methodName, parameterDescription, args);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        after.after(target, className, methodName, parameterDescription, args, result, throwable);
    }


}
