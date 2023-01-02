/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextUtils;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

/**
 * @author Taejin Koo
 */
public class ServerHalfCloseListenerInterceptor extends ServerListenerInterceptor {

    public ServerHalfCloseListenerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void finishAsyncState(final AsyncContext asyncContext) {
        if (AsyncContextUtils.asyncStateFinish(asyncContext)) {
            if (isDebug) {
                logger.debug("finished asyncState. asyncTraceId={}", asyncContext);
            }
        }
    }


}
