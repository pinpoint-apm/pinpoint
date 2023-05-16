/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.flink.receiver;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.FlinkTBaseLocator;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class TcpDispatchHandler<REQ, RES> implements DispatchHandler<REQ, RES> {

    private SimpleHandler<REQ> simpleHandler;

    private SimpleHandler<REQ> getSimpleHandler(Header header) {
        if (header.getType() == FlinkTBaseLocator.AGENT_STAT_BATCH) {
            return simpleHandler;
        }
        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    private SimpleHandler<REQ> getSimpleHandler(ServerRequest<REQ> serverRequest) {
        final Header header = serverRequest.getHeader();
        return getSimpleHandler(header);
    }

    public void setSimpletHandler(SimpleHandler<REQ> simpleHandler) {
        this.simpleHandler = Objects.requireNonNull(simpleHandler, "agentStatHandler");
    }

    @Override
    public void dispatchSendMessage(ServerRequest<REQ> serverRequest) {
        SimpleHandler<REQ> simpleHandler = getSimpleHandler(serverRequest);
        simpleHandler.handleSimple(serverRequest);
    }

    @Override
    public void dispatchRequestMessage(ServerRequest<REQ> serverRequest, ServerResponse<RES> serverResponse) {
    }
}
