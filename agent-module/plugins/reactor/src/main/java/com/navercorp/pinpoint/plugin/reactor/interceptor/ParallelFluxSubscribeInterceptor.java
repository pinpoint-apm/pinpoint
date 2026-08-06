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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;

public class ParallelFluxSubscribeInterceptor implements InjectedAsyncContextApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ParallelFluxSubscribeInterceptor() {
    }

    @Override
    public void before(Object target, AsyncContext asyncContext, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (args == null) {
            return;
        }

        try {
            // asyncContext is supplied by the weaver (monomorphic getfield of the injected accessor field).
            if (asyncContext != null) {
                setAsyncContext(asyncContext, args);
                if (isDebug) {
                    logger.debug("Set asyncContext to args. asyncContext={}", asyncContext);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, AsyncContext asyncContext, int apiId, Object[] args, Object result, Throwable throwable) {
    }

    private void setAsyncContext(final AsyncContext asyncContext, final Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Object[]) {
                final Object[] array = (Object[]) arg;
                for (Object object : array) {
                    AsyncContextAccessorUtils.setAsyncContext(asyncContext, object);
                }
            } else {
                AsyncContextAccessorUtils.setAsyncContext(asyncContext, arg);
            }
        }
    }
}
