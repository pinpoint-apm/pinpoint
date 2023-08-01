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
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;

import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class AgentDispatchHandler<REQ, RES> implements DispatchHandler<REQ, RES> {

    private final SimpleAndRequestResponseHandler<REQ, RES> agentInfoHandler;

    private final RequestResponseHandler<REQ, RES> sqlMetaDataHandler;

    private final RequestResponseHandler<REQ, RES> apiMetaDataHandler;

    private final RequestResponseHandler<REQ, RES> stringMetaDataHandler;

    private final RequestResponseHandler<REQ, RES> exceptionMetaDataHandler;

    public AgentDispatchHandler(final SimpleAndRequestResponseHandler<REQ, RES> agentInfoHandler,
                                final RequestResponseHandler<REQ, RES> sqlMetaDataHandler,
                                final RequestResponseHandler<REQ, RES> apiMetaDataHandler,
                                final RequestResponseHandler<REQ, RES> stringMetaDataHandler,
                                final RequestResponseHandler<REQ, RES> exceptionMetaDataHandler) {
        this.agentInfoHandler = Objects.requireNonNull(agentInfoHandler, "agentInfoHandler");
        this.sqlMetaDataHandler = Objects.requireNonNull(sqlMetaDataHandler, "sqlMetaDataHandler");
        this.apiMetaDataHandler = Objects.requireNonNull(apiMetaDataHandler, "apiMetaDataHandler");
        this.stringMetaDataHandler = Objects.requireNonNull(stringMetaDataHandler, "stringMetaDataHandler");
        this.exceptionMetaDataHandler = Objects.requireNonNull(exceptionMetaDataHandler, "exceptionMetaDataHandler");
    }

    protected RequestResponseHandler<REQ, RES> getRequestResponseHandler(ServerRequest<? extends REQ> serverRequest) {
        final Header header = serverRequest.getHeader();
        final short type = header.getType();
        switch (type) {
            case DefaultTBaseLocator.SQLMETADATA:
            case DefaultTBaseLocator.SQLUIDMETADATA:
                return sqlMetaDataHandler;
            case DefaultTBaseLocator.APIMETADATA:
                return apiMetaDataHandler;
            case DefaultTBaseLocator.STRINGMETADATA:
                return stringMetaDataHandler;
            case DefaultTBaseLocator.AGENT_INFO:
                return agentInfoHandler;
            case DefaultTBaseLocator.EXCEPTIONMETADATA:
                return exceptionMetaDataHandler;
        }
        throw new UnsupportedOperationException("unsupported header:" + header);
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