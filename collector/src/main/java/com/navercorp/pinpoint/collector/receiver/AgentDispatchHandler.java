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

import com.navercorp.pinpoint.collector.handler.SimpleAndRequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
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
public class AgentDispatchHandler<T> implements DispatchHandler<T> {

    private final SimpleAndRequestResponseHandler<T> agentInfoHandler;

    private final RequestResponseHandler<T> sqlMetaDataHandler;

    private final RequestResponseHandler<T> apiMetaDataHandler;

    private final RequestResponseHandler<T> stringMetaDataHandler;

    public AgentDispatchHandler(final SimpleAndRequestResponseHandler<T> agentInfoHandler,
                                final RequestResponseHandler<T> sqlMetaDataHandler,
                                final RequestResponseHandler<T> apiMetaDataHandler,
                                final RequestResponseHandler<T> stringMetaDataHandler) {
        this.agentInfoHandler = Objects.requireNonNull(agentInfoHandler, "agentInfoHandler");
        this.sqlMetaDataHandler = Objects.requireNonNull(sqlMetaDataHandler, "sqlMetaDataHandler");
        this.apiMetaDataHandler = Objects.requireNonNull(apiMetaDataHandler, "apiMetaDataHandler");
        this.stringMetaDataHandler = Objects.requireNonNull(stringMetaDataHandler, "stringMetaDataHandler");
    }

    protected RequestResponseHandler<T> getRequestResponseHandler(ServerRequest<T> serverRequest) {
        final Header header = serverRequest.getHeader();
        final short type = header.getType();
        switch (type) {
            case DefaultTBaseLocator.SQLMETADATA:
                return sqlMetaDataHandler;
            case DefaultTBaseLocator.APIMETADATA:
                return apiMetaDataHandler;
            case DefaultTBaseLocator.STRINGMETADATA:
                return stringMetaDataHandler;
            case DefaultTBaseLocator.AGENT_INFO:
                return agentInfoHandler;
        }
        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    private SimpleHandler<T> getSimpleHandler(Header header) {
        final short type = header.getType();
        if (type == DefaultTBaseLocator.AGENT_INFO) {
            return agentInfoHandler;
        }

        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    @Override
    public void dispatchSendMessage(ServerRequest<T> serverRequest) {
        final Header header = serverRequest.getHeader();
        SimpleHandler<T> simpleHandler = getSimpleHandler(header);
        simpleHandler.handleSimple(serverRequest);
    }

    @Override
    public void dispatchRequestMessage(ServerRequest<T> serverRequest, ServerResponse<T> serverResponse) {
        RequestResponseHandler<T> requestResponseHandler = getRequestResponseHandler(serverRequest);
        requestResponseHandler.handleRequest(serverRequest, serverResponse);
    }
}