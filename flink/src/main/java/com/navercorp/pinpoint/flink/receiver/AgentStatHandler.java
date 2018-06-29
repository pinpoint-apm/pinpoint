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
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.DefaultServerRequest;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class AgentStatHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SourceContext sourceContext;

    public AgentStatHandler(SourceContext sourceContext) {
        this.sourceContext = Objects.requireNonNull(sourceContext, "sourceContext must not be null");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (!(data instanceof TBase<?, ?>)) {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }

        ServerRequest<TBase<?, ?>> copiedServerRequest = copyServerRequest(serverRequest);

        sourceContext.collect(copiedServerRequest);
    }

    private ServerRequest<TBase<?, ?>> copyServerRequest(ServerRequest serverRequest) {
        Header header = serverRequest.getHeader();
        Header copiedHeader = copyHeader(header);

        TBase<?, ?> data = (TBase<?, ?>) serverRequest.getData();
        Message<TBase<?, ?>> message = new DefaultMessage<>(copiedHeader, data);
        return new DefaultServerRequest<>(message, serverRequest.getRemoteAddress(), serverRequest.getRemotePort());
    }

    private Header copyHeader(Header header) {
        if (header.getVersion() == HeaderV1.VERSION) {
            return header;
        }
        if (header.getVersion() == HeaderV2.VERSION) {
            return new HeaderV2(header.getSignature(), header.getVersion(), header.getType(), new HashMap<>(header.getHeaderData()));
        }

        throw new IllegalStateException("unsupported header version " + header);

    }


}
