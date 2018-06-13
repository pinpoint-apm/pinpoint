/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.flink.receiver;


import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.UnSupportedServerRequestTypeException;
import com.navercorp.pinpoint.thrift.dto.ThriftRequest;
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

    private final Logger logger = LoggerFactory.getLogger(AgentStatHandler.class);
    private final SourceContext sourceContext;

    public AgentStatHandler(SourceContext sourceContext) {
        this.sourceContext = Objects.requireNonNull(sourceContext, "sourceContext must not be null");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        if (!(serverRequest instanceof ThriftRequest)) {
            throw new UnSupportedServerRequestTypeException(serverRequest.getClass() + "is not support type : " + serverRequest);
        }

        ThriftRequest thriftRequest = (ThriftRequest) serverRequest;
        ThriftRequest copiedThriftRequest = copyThriftRequest(thriftRequest);

        if (copiedThriftRequest == null) {
            logger.error("can not copy thriftRequest : " + thriftRequest);
            return;
        }

        sourceContext.collect(copiedThriftRequest);
    }

    private ThriftRequest copyThriftRequest(ThriftRequest thriftRequest) {
        Header header = thriftRequest.getHeader();
        Header copiedHeader = null;

        if (header.getVersion() == HeaderV1.VERSION) {
            copiedHeader = header;
        } else if (header.getVersion() == HeaderV2.VERSION) {
            copiedHeader = new HeaderV2(header.getSignature(), header.getVersion(), header.getType(), new HashMap<>(header.getHeaderData()));
        } else {
            return null;
        }

        return new ThriftRequest(copiedHeader, thriftRequest.getData());
    }

    @Override
    public void handleSimple(TBase<?, ?> tBase) {
        sourceContext.collect(tBase);
    }

}
