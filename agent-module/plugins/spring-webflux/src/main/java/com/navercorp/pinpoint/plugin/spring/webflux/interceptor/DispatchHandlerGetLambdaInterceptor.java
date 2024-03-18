/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

public class DispatchHandlerGetLambdaInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();


    public DispatchHandlerGetLambdaInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        if (Boolean.FALSE == (target instanceof AsyncContextAccessor)) {
            return;
        }

        if (args == null) {
            return;
        }

        if (args.length == 1) {
            setAsyncContextToTarget(args[0], target);
        } else if (args.length == 2) {
            setAsyncContextToTarget(args[1], target);
        }
    }

    private void setAsyncContextToTarget(final Object arg, final Object target) {
        // org.springframework.web.server.ServerWebExchange
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(arg);
        if (asyncContext != null) {
            ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext to target. asyncContext={}", asyncContext);
            }
        }
    }
}
