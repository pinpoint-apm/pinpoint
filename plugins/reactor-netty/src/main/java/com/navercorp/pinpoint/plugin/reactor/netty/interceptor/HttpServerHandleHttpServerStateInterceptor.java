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

import reactor.netty.ConnectionObserver;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerState;

/**
 * @author jaehong.kim
 */
public class HttpServerHandleHttpServerStateInterceptor extends AbstractHttpServerHandleInterceptor {

    public HttpServerHandleHttpServerStateInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<HttpServerRequest> requestRecorderFactory) {
        super(traceContext, descriptor, requestRecorderFactory);
    }

    public boolean validate(Object[] args) {
        if (args == null || args.length < 2) {
            return false;
        }

        if (!(args[1] instanceof ConnectionObserver.State)) {
            return false;
        }

        return true;
    }

    public boolean isReceived(Object[] args) {
        if (!validate(args)) {
            return false;
        }
        ConnectionObserver.State state = (ConnectionObserver.State) args[1];
        if (state != HttpServerState.REQUEST_RECEIVED) {
            return false;
        }
        return true;
    }

    public boolean isDisconnecting(Object[] args) {
        if (!validate(args)) {
            return false;
        }
        ConnectionObserver.State state = (ConnectionObserver.State) args[1];
        if (state != HttpServerState.DISCONNECTING) {
            return false;
        }
        return true;
    }
}