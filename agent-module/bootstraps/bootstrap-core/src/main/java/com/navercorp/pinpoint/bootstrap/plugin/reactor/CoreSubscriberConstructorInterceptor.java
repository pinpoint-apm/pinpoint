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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * Copies the AsyncContext of the subscriber being wrapped ("actual", passed as a constructor argument)
 * onto the operator subscriber under construction, so the carrier travels up the chain as the chain is
 * built.
 */
public class CoreSubscriberConstructorInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    /**
     * Interceptor code for a constructor is woven in right after the super()/this() call, so target is a
     * real (already super-initialized) instance and the injected field can be written here. Copying the
     * carrier at this point - rather than once the constructor has returned - matters for inner
     * subscribers that an operator subscriber creates inside its own constructor body, e.g.
     * FluxConcatMap$ConcatMapInner: by the time the body runs the enclosing subscriber already carries
     * the AsyncContext, so the inner copies it like any other subscriber.
     */
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final AsyncContext asyncContext = ReactorAsyncContextResolver.findUnique(args);
            if (asyncContext == null) {
                return;
            }

            AsyncContextAccessorUtils.setAsyncContext(asyncContext, target);
            if (isDebug) {
                logger.debug("Copy asyncContext from actual(parent). asyncContext={}", asyncContext);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    /**
     * Not woven in - see {@link IgnoreMethod}. before() has already copied the carrier, and a
     * constructor is called for every subscriber of every instrumented operator, so the saved
     * interceptor call and argument array scan are worth having.
     */
    @IgnoreMethod
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do nothing
    }

    /**
     * @deprecated constructor propagation needs the AsyncContext itself and must account for multiple
     * accessors. Kept for compatibility with callers of the former helper.
     */
    @Deprecated
    public static AsyncContextAccessor findActual(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        final int length = args.length - 1;
        for (int i = 0; i <= length; i++) {
            final Object arg = args[i];
            if (arg instanceof AsyncContextAccessor) {
                return (AsyncContextAccessor) arg;
            }
        }
        return null;
    }
}
