/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.test.pinpoint.jdk8.interfaces;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

import java.util.Arrays;

/**
 * @author jaehong.kim
 */
public class SimpleInterceptor implements AroundInterceptor {

    public static boolean before;
    public static boolean after;
    public static Object beforeTarget;
    public static Object[] beforeArgs;
    public static Object afterTarget;
    public static Object[] afterArgs;
    public static Object result;
    public static Throwable throwable;

    public static void clear() {
        before = false;
        after = false;
        beforeTarget = null;
        beforeArgs = null;
        afterTarget = null;
        afterArgs = null;
        result = null;
        throwable = null;
    }

    @Override
    public void before(Object target, Object[] args) {
        this.before = true;
        this.beforeTarget = target;
        this.beforeArgs = args;
        System.out.println("before target=" + target + ", args=" + toArgs(args));
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        this.after = true;
        this.afterTarget = target;
        this.afterArgs = args;
        this.result = result;
        this.throwable = throwable;
        System.out.println("after target=" + target + ", args=" + toArgs(args) + ", result=" + result + ", throwable=" + throwable);
    }

    private String toArgs(Object[] args) {
        if (args == null) {
            return "null";
        }

        return Arrays.asList(args).toString();
    }
}