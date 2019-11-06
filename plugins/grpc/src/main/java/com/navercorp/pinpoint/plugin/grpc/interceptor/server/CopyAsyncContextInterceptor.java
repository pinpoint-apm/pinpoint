/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc.interceptor.server;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.ServerStreamGetter;
import io.grpc.internal.ServerStream;

/**
 * @author Taejin Koo
 */
public class CopyAsyncContextInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(CopyAsyncContextInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    public CopyAsyncContextInterceptor() {
    }

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

        if (ArrayUtils.getLength(args) == 2) {
            AsyncContext asyncContext = getAsyncContext(args[0]);

            if (result instanceof AsyncContextAccessor) {
                logger.info("set AsyncContext:{}", asyncContext);
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
    }

    AsyncContext getAsyncContext(Object object) {
        if (object instanceof ServerStreamGetter) {
            ServerStream serverStream = ((ServerStreamGetter) object)._$PINPOINT$_getServerStream();
            if (serverStream instanceof AsyncContextAccessor) {
                return ((AsyncContextAccessor) serverStream)._$PINPOINT$_getAsyncContext();
            }
        }
        return null;
    }

}
