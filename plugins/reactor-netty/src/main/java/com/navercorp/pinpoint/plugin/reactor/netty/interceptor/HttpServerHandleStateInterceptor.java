/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;

import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.server.HttpServerRequest;

/**
 * @author jaehong.kim
 */
public class HttpServerHandleStateInterceptor extends AbstractHttpServerHandleInterceptor {

    public HttpServerHandleStateInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<HttpServerRequest> requestRecorderFactory) {
        super(traceContext, descriptor, requestRecorderFactory);
    }

    public boolean isReceived(Object[] args) {
        final ConnectionObserver.State state = ArrayArgumentUtils.getArgument(args, 1, ConnectionObserver.State.class);
        if (state != null && state == ConnectionObserver.State.CONFIGURED) {
            return true;
        }
        return false;
    }

    public boolean isClosed(Object[] args) {
        final ConnectionObserver.State state = ArrayArgumentUtils.getArgument(args, 1, ConnectionObserver.State.class);
        if (state == null) {
            return false;
        }
        // ACQUIRED: Propagated when a connection has been reused / acquired (keep-alive or pooling)
        // RELEASED: Propagated when a connection has been released but not fully closed (keep-alive or pooling)
        // DISCONNECTING: Propagated when a connection is being fully closed
        if (state == ConnectionObserver.State.DISCONNECTING || state == ConnectionObserver.State.ACQUIRED || state == ConnectionObserver.State.RELEASED) {
            return true;
        }
        return false;
    }
}