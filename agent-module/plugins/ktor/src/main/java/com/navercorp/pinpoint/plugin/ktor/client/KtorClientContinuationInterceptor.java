/*
 * Copyright 2026 NAVER Corp.
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


package com.navercorp.pinpoint.plugin.ktor.client;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

public class KtorClientContinuationInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
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
            logger.afterInterceptor(target, args);
        }

        if (KtorClientCoroutineSuspendedMarker.isSuspended(result)) {
            return;
        }

        KtorClientTraceHolder holder = getHolder(target);
        if (holder == null) {
            return;
        }

        try {
            try {
                holder.finishAsync(throwable);
            } catch (Throwable finishThrowable) {
                logger.warn(
                        "Failed to finish Ktor client trace in continuation interceptor. {}",
                        finishThrowable.getMessage(),
                        finishThrowable
                );
            }
        } finally {
            if (target instanceof KtorClientTraceAccessor) {
                ((KtorClientTraceAccessor) target)._$PINPOINT$_setKtorClientTrace(null);
            }
        }
    }

    private KtorClientTraceHolder getHolder(Object target) {
        if (target instanceof KtorClientTraceAccessor) {
            return ((KtorClientTraceAccessor) target)._$PINPOINT$_getKtorClientTrace();
        }
        return null;
    }
}
