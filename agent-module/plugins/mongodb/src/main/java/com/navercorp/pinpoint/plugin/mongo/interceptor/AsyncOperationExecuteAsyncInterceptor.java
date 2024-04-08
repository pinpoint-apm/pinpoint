/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

import java.util.Objects;

public class AsyncOperationExecuteAsyncInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public AsyncOperationExecuteAsyncInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        Objects.requireNonNull(traceContext, "traceContext");
        Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            final AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 1, AsyncContextAccessor.class);
            if (asyncContextAccessor != null) {
                asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set asyncContext to args[1]. asyncContext={}", asyncContext);
                }
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
