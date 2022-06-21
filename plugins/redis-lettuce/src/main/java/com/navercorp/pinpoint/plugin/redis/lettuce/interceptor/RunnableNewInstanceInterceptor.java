/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.lettuce.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessorUtils;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import org.reactivestreams.Subscriber;

public class RunnableNewInstanceInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public RunnableNewInstanceInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        Subscriber subscriber = ArrayArgumentUtils.getArgument(args, 1, Subscriber.class);
        if (subscriber == null) {
            subscriber = ArrayArgumentUtils.getArgument(args, 0, Subscriber.class);
        }

        if (subscriber == null) {
            return;
        }

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(subscriber);
        if (asyncContext == null) {
            asyncContext = ReactorContextAccessorUtils.getAsyncContext(subscriber);
        }
        if (asyncContext == null) {
            return;
        }

        // Set result to asyncContext
        if (throwable != null) {
            AsyncContextAccessorUtils.setAsyncContext(asyncContext, result);
        }
    }
}
