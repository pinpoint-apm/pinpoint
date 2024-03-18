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

package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceAccessor;

/**
 * @author HyunGil Jeong
 */
public class HystrixObservableTimeoutListenerConstructorInterceptor implements AroundInterceptor {


    public HystrixObservableTimeoutListenerConstructorInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final Object asyncContextAccessor = ArrayUtils.get(args, 0);
        if (asyncContextAccessor instanceof AsyncContextAccessor && target instanceof EnclosingInstanceAccessor) {
            ((EnclosingInstanceAccessor) target)._$PINPOINT$_setEnclosingInstance(args[0]);
        }
    }
}
