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

public class FluxAndMonoOperatorConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public FluxAndMonoOperatorConstructorInterceptor() {
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
            return;
        }

        try {
            // source
            if (checkSourceReactorContextAccessor(target, args)) {
                return;
            }
            if (checkSourceAsyncContextAccessor(target, args)) {
                return;
            }
            // args
            checkArgsAsyncContextAccessor(target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }

    boolean checkSourceReactorContextAccessor(final Object target, final Object[] args) {
        final AsyncContext sourceAsyncContext = getReactorContextFromSource(target, args);
        if (sourceAsyncContext != null) {
            setReactorContextToTarget(sourceAsyncContext, target);
            return true;
        }
        return false;
    }

    boolean checkSourceAsyncContextAccessor(final Object target, final Object[] args) {
        final AsyncContext sourceAsyncContext = getAsyncContextFromSource(target, args);
        if (sourceAsyncContext != null) {
            setReactorContextToTarget(sourceAsyncContext, target);
            return true;
        }
        return false;
    }

    boolean checkArgsAsyncContextAccessor(final Object target, final Object[] args) {
        final AsyncContext argAsyncContext = getAsyncContextFromArgs(target, args);
        if (argAsyncContext != null) {
            setAsyncContextToTarget(argAsyncContext, target);
            setReactorContextToTarget(argAsyncContext, target);
            return true;
        }
        return false;
    }

    protected AsyncContext getReactorContextFromSource(final Object target, final Object[] args) {
        return ReactorContextAccessorUtils.getAsyncContext(args, 0);
    }

    protected AsyncContext getAsyncContextFromSource(final Object target, final Object[] args) {
        return AsyncContextAccessorUtils.getAsyncContext(args, 0);
    }

    protected AsyncContext getAsyncContextFromArgs(final Object target, final Object[] args) {
        return AsyncContextAccessorUtils.findAsyncContext(args, 1);
    }

    protected void setAsyncContextToTarget(AsyncContext asyncContext, Object target) {
        AsyncContextAccessorUtils.setAsyncContext(asyncContext, target);
    }

    // Trace reactor
    protected void setReactorContextToTarget(AsyncContext asyncContext, Object target) {
        ReactorContextAccessorUtils.setAsyncContext(asyncContext, target);
    }
}