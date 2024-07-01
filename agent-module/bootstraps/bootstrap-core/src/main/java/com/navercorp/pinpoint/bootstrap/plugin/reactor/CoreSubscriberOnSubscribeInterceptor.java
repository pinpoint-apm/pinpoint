/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

public class CoreSubscriberOnSubscribeInterceptor implements AroundInterceptor {

    public CoreSubscriberOnSubscribeInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        final AsyncContext thisAsyncContext = ReactorContextAccessorUtils.getAsyncContext(target);
        final AsyncContext subscriptionAsyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        if (thisAsyncContext != null) {
            if (subscriptionAsyncContext == null) {
                ReactorContextAccessorUtils.setAsyncContext(thisAsyncContext, args, 0);
            }
        } else {
            if (subscriptionAsyncContext != null) {
                ReactorContextAccessorUtils.setAsyncContext(subscriptionAsyncContext, target);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
