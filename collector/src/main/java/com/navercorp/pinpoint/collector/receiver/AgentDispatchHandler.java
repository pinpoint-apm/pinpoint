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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleAndRequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class AgentDispatchHandler<REQ, RES> implements DispatchHandler<REQ, RES> {

    private final Logger logger = LogManager.getLogger(getClass());

    private final SimpleAndRequestResponseHandler<REQ, RES> agentInfoHandler;

    private final IntHashMap<RequestResponseHandler<REQ, RES>> handlerMap;


    public AgentDispatchHandler(final SimpleAndRequestResponseHandler<REQ, RES> agentInfoHandler,
                                List<RequestResponseHandler<REQ, RES>> handlers) {
        this.agentInfoHandler = Objects.requireNonNull(agentInfoHandler, "agentInfoHandler");

        handlers.forEach(handler -> logger.info("{} {}", handler.type(), handler.getClass()));

        this.handlerMap = new IntHashMap<>();
        for (RequestResponseHandler<REQ, RES> handler : handlers) {
            RequestResponseHandler<REQ, RES> old = handlerMap.put(handler.type(), handler);
            if (old != null) {
                throw new IllegalArgumentException("Unexpected type new:" + handler + " old:" + old);
            }
        }

    }

    protected RequestResponseHandler<REQ, RES> getRequestResponseHandler(ServerRequest<? extends REQ> serverRequest) {
        final Header header = serverRequest.getHeader();
        final short type = header.getType();
        RequestResponseHandler<REQ, RES> handler = this.handlerMap.get(type);
        if (handler == null) {
            throw new UnsupportedOperationException("unsupported header:" + header);
        }
        return handler;
    }

    private SimpleHandler<REQ> getSimpleHandler(Header header) {
        final short type = header.getType();
        if (type == DefaultTBaseLocator.AGENT_INFO) {
            return agentInfoHandler;
        }

        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    @Override
    public void dispatchSendMessage(ServerRequest<REQ> serverRequest) {
        final Header header = serverRequest.getHeader();
        SimpleHandler<REQ> simpleHandler = getSimpleHandler(header);
        simpleHandler.handleSimple(serverRequest);
    }

    @Override
    public void dispatchRequestMessage(ServerRequest<REQ> serverRequest, ServerResponse<RES> serverResponse) {
        RequestResponseHandler<REQ, RES> requestResponseHandler = getRequestResponseHandler(serverRequest);
        requestResponseHandler.handleRequest(serverRequest, serverResponse);
    }
}