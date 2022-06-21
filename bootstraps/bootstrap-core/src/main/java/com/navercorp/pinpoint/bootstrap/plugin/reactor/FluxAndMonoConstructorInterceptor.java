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
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

public class FluxAndMonoConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            return;
        }

        try {
            // Check arguments
            final AsyncContext argAsyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 0);
            if (argAsyncContext != null) {
                setReactorContextToTarget(argAsyncContext, target);
                setAsyncContextToTarget(argAsyncContext, target);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }

    // Trace reactor
    protected void setReactorContextToTarget(final AsyncContext asyncContext, final Object target) {
        final AsyncContext targetAsyncContext = ReactorContextAccessorUtils.getAsyncContext(target);
        if (targetAsyncContext == null) {
            ReactorContextAccessorUtils.setAsyncContext(asyncContext, target);
        }
    }

    // Trace subscribe() method
    protected void setAsyncContextToTarget(AsyncContext asyncContext, Object target) {
        final AsyncContext targetAsyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (targetAsyncContext == null) {
            AsyncContextAccessorUtils.setAsyncContext(asyncContext, target);
        }
    }
}
