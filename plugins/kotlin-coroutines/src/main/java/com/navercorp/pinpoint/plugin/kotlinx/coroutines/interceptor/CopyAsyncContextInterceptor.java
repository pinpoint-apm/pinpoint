/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * @author Taejin Koo
 */
public class CopyAsyncContextInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(CopyAsyncContextInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (ArrayUtils.isEmpty(args)) {
            return;
        }

        AsyncContext originalAsyncContext = getDelegateAsyncContext(args[0]);
        if (originalAsyncContext == null) {
            logger.warn("Could not find delegate's AsyncContext");
            return;
        }

        setAsyncContext(target, originalAsyncContext);
    }

    private AsyncContext getDelegateAsyncContext(Object asyncContextAccessor) {
        if (asyncContextAccessor instanceof AsyncContextAccessor) {
            return ((AsyncContextAccessor) asyncContextAccessor)._$PINPOINT$_getAsyncContext();
        }
        return null;
    }

    private void setAsyncContext(Object target, AsyncContext asyncContext) {
        if (target instanceof AsyncContextAccessor) {
            AsyncContext hasValue = ((AsyncContextAccessor) target)._$PINPOINT$_getAsyncContext();
            if (hasValue == null) {
                ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
            } else {
                logger.warn("Target already has AsyncContext.");
            }
        }
    }


}
