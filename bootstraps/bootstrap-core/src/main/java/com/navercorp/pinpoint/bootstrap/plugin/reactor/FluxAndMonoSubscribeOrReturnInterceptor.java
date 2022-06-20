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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

public class FluxAndMonoSubscribeOrReturnInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public FluxAndMonoSubscribeOrReturnInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            // Ignore if an error occurs.
            return;
        }
        if (result == null) {
            return;
        }

        if (checkTargetReactorContextAccessor(target, args, result)) {
            return;
        }
        if (checkTargetAsyncContextAccessor(target, args, result)) {
            return;
        }
        if (checkSubscriberReactorContextAccessor(target, args, result)) {
            return;
        }
    }

    boolean checkTargetReactorContextAccessor(final Object target, final Object[] args, final Object result) {
        final AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            setReactorContextToResult(asyncContext, result);
            return true;
        }
        return false;
    }

    boolean checkTargetAsyncContextAccessor(final Object target, final Object[] args, final Object result) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            setReactorContextToResult(asyncContext, result);
            return true;
        }
        return false;
    }

    boolean checkSubscriberReactorContextAccessor(final Object target, final Object[] args, final Object result) {
        final AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext != null) {
            setReactorContextToResult(asyncContext, result);
            return true;
        }
        return false;
    }

    protected void setReactorContextToResult(AsyncContext asyncContext, Object result) {
        ReactorContextAccessorUtils.setAsyncContext(asyncContext, result);
    }
}
