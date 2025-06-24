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
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.io.util.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class AgentDispatchHandler<REQ, RES> implements DispatchHandler<REQ, RES> {

    private final Logger logger = LogManager.getLogger(getClass());

    private final IntHashMap<RequestResponseHandler<REQ, RES>> handlerMap;


    public AgentDispatchHandler(List<RequestResponseHandler<REQ, RES>> handlers) {

        handlers.forEach(handler -> logger.info("AgentDispatchHandler {} {}", handler.type(), handler.getClass()));

        this.handlerMap = new IntHashMap<>();
        for (RequestResponseHandler<REQ, RES> handler : handlers) {
            RequestResponseHandler<REQ, RES> old = handlerMap.put(handler.type().getCode(), handler);
            if (old != null) {
                throw new IllegalArgumentException("Unexpected type new:" + handler + " old:" + old);
            }
        }

    }

    protected RequestResponseHandler<REQ, RES> getRequestResponseHandler(ServerRequest<REQ> serverRequest) {
        MessageType messageType = serverRequest.getMessageType();
        final int type = messageType.getCode();
        RequestResponseHandler<REQ, RES> handler = this.handlerMap.get(type);
        if (handler == null) {
            throw new UnsupportedOperationException("unsupported header:" + messageType);
        }
        return handler;
    }

    @Override
    public void dispatchSendMessage(ServerRequest<REQ> serverRequest) {
        throw new UnsupportedOperationException("unsupported header:" + serverRequest.getHeader());
    }

    @Override
    public void dispatchRequestMessage(ServerRequest<REQ> serverRequest, ServerResponse<RES> serverResponse) {
        RequestResponseHandler<REQ, RES> requestResponseHandler = getRequestResponseHandler(serverRequest);
        requestResponseHandler.handleRequest(serverRequest, serverResponse);
    }
}