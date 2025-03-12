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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriber;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriberAccessorUtils;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

public class FluxAndMonoSubscribeMethodInterceptor implements BlockApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final boolean traceSubscribe;

    public FluxAndMonoSubscribeMethodInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
        final ReactorPluginConfig config = new ReactorPluginConfig(traceContext.getProfilerConfig());
        this.traceSubscribe = config.isTraceSubscribe();
    }

    @Override
    public TraceBlock before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (Boolean.FALSE == traceSubscribe) {
            return null;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return null;
        }

        if (ReactorSubscriberAccessorUtils.get(args, 0) != null) {
            // already set reactorSubscriber
            return null;
        }

        try {
            final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
            if (asyncContext != null) {
                final ReactorSubscriber reactorSubscriber = new ReactorSubscriber(asyncContext);
                ReactorSubscriberAccessorUtils.set(reactorSubscriber, args, 0);
                if (isDebug) {
                    logger.debug("Pass this to subscribe(args[0]). reactorSubscriber={}", reactorSubscriber);
                }
                return null;
            }

            final TraceBlock traceBlock = trace.traceBlockBeginAndGet();
            final AsyncContext nextAsyncContext = traceBlock.recordNextAsyncContext();
            // set reactorSubscriber to args[0]
            final ReactorSubscriber reactorSubscriber = new ReactorSubscriber(nextAsyncContext);
            ReactorSubscriberAccessorUtils.set(reactorSubscriber, args, 0);
            if (isDebug) {
                logger.debug("Set reactorSubscriber to args[0]. reactorSubscriber={}", reactorSubscriber);
            }
            return traceBlock;
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }

        return null;
    }

    @Override
    public void after(TraceBlock block, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (block == null) {
            return;
        }

        final Trace trace = block.getTrace();
        if (trace == null) {
            return;
        }

        try (final TraceBlock traceBlock = block) {
            traceBlock.recordApiId(apiId);
            traceBlock.recordServiceType(ReactorConstants.REACTOR);
            traceBlock.recordException(throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }
}