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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

/**
 * Links the AsyncContext of a subscriber with its upstream Subscription, so that whichever of the two
 * already carries the context shares it with the other.
 */
public class CoreSubscriberOnSubscribeInterceptor implements InjectedAsyncContextApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public CoreSubscriberOnSubscribeInterceptor() {
    }

    @Override
    public void before(Object target, AsyncContext ownAsyncContext, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            // ownAsyncContext is this subscriber's own context, supplied by the weaver
            // (monomorphic getfield of the injected accessor field).
            if (ownAsyncContext != null) {
                // The carrier is already here - CoreSubscriberConstructorInterceptor copied it from the
                // actual while the chain was built - so only the upstream Subscription may be missing it.
                if (AsyncContextAccessorUtils.getAsyncContext(args, 0) == null) {
                    AsyncContextAccessorUtils.setAsyncContext(ownAsyncContext, args, 0);
                    if (isDebug) {
                        logger.debug("Pass this to subscription(args[0]). asyncContext={}", ownAsyncContext);
                    }
                }
                return;
            }

            // The carrier has not reached this subscriber yet - take it from the upstream Subscription.
            // Nothing is pushed down to the actual here: the actual receives this subscriber as its own
            // Subscription right after, and picks the carrier up through this same path.
            final AsyncContext subscriptionAsyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
            if (subscriptionAsyncContext == null) {
                return;
            }

            AsyncContextAccessorUtils.setAsyncContext(subscriptionAsyncContext, target);
            if (isDebug) {
                logger.debug("Set asyncContext to this. asyncContext={}", subscriptionAsyncContext);
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
}
