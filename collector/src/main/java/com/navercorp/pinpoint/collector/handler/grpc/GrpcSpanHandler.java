/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class GrpcSpanHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TraceService traceService;

    private GrpcSpanFactory spanFactory;

    @Autowired
    public GrpcSpanHandler(TraceService traceService, GrpcSpanFactory spanFactory) {
        this.traceService = Objects.requireNonNull(traceService, "traceService must not be null");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory must not be null");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle simple data={}", data);
        }

        if (data instanceof PSpan) {
            handleSpan((PSpan) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }

    private void handleSpan(PSpan pSpan) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PSpan={}", pSpan);
        }

        try {
            AgentHeaderFactory.Header agentInfo = ServerContext.getAgentInfo();
            final SpanBo spanBo = spanFactory.buildSpanBo(pSpan, agentInfo);
            traceService.insertSpan(spanBo);
        } catch (Exception e) {
            logger.warn("Span handle error. Caused:{}. Span:{}", e.getMessage(), pSpan, e);
        }
    }
}