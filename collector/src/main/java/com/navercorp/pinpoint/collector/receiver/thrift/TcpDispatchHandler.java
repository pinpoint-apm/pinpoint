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

package com.navercorp.pinpoint.collector.receiver.thrift;

import com.navercorp.pinpoint.collector.handler.thrift.ThriftAgentInfoHandler;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.handler.thrift.ThriftApiMetaDataHandler;
import com.navercorp.pinpoint.collector.handler.thrift.ThriftSqlMetaDataHandler;
import com.navercorp.pinpoint.collector.handler.thrift.ThriftStringMetaDataHandler;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class TcpDispatchHandler implements DispatchHandler {

    @Autowired
    private ThriftAgentInfoHandler thriftAgentInfoHandler;

    @Autowired
    private ThriftSqlMetaDataHandler thriftSqlMetaDataHandler;

    @Autowired
    private ThriftApiMetaDataHandler thriftApiMetaDataHandler;

    @Autowired
    private ThriftStringMetaDataHandler thriftStringMetaDataHandler;

    public TcpDispatchHandler() {
    }

    protected RequestResponseHandler getRequestResponseHandler(ServerRequest serverRequest) {
        final Header header = serverRequest.getHeader();
        final short type = header.getType();
        if (type == DefaultTBaseLocator.SQLMETADATA) {
            return thriftSqlMetaDataHandler;
        }
        if (type == DefaultTBaseLocator.APIMETADATA) {
            return thriftApiMetaDataHandler;
        }
        if (type == DefaultTBaseLocator.STRINGMETADATA) {
            return thriftStringMetaDataHandler;
        }
        if (type == DefaultTBaseLocator.AGENT_INFO) {
            return thriftAgentInfoHandler;
        }
        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    private SimpleHandler getSimpleHandler(Header header) {
        final short type = header.getType();
        if (type == DefaultTBaseLocator.AGENT_INFO) {
            return thriftAgentInfoHandler;
        }

        throw new UnsupportedOperationException("unsupported header:" + header);
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        final Header header = serverRequest.getHeader();
        SimpleHandler simpleHandler = getSimpleHandler(header);
        simpleHandler.handleSimple(serverRequest);
    }

    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        RequestResponseHandler requestResponseHandler = getRequestResponseHandler(serverRequest);
        requestResponseHandler.handleRequest(serverRequest, serverResponse);
    }

}
