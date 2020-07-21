/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.collector.mapper.grpc.metric.GrpcAgentCustomMetricMapper;
import com.navercorp.pinpoint.collector.service.AgentCustomMetricDispatchService;
import com.navercorp.pinpoint.common.server.bo.metric.AgentCustomMetricBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PCustomMetricMessage;
import com.navercorp.pinpoint.io.request.ServerRequest;

import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class GrpcAgentCustomMetricHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final GrpcAgentCustomMetricMapper agentCustomMetricMapper;

    private final AgentCustomMetricDispatchService agentCustomMetricDispatchService;

    public GrpcAgentCustomMetricHandler(GrpcAgentCustomMetricMapper agentCustomMetricMapper, AgentCustomMetricDispatchService agentCustomMetricDispatchService) {
        this.agentCustomMetricMapper = Objects.requireNonNull(agentCustomMetricMapper, "agentCustomMetricMapper");
        this.agentCustomMetricDispatchService = Objects.requireNonNull(agentCustomMetricDispatchService, "agentCustomMetricDispatchService");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof PCustomMetricMessage) {
            handleAgentCustomMetric((PCustomMetricMessage) data);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private void handleAgentCustomMetric(PCustomMetricMessage customMetricMessage) {
        if (isDebug) {
            logger.debug("Handle PAgentStatBatch={}", MessageFormatUtils.debugLog(customMetricMessage));
        }

        final Header header = ServerContext.getAgentInfo();
        final AgentCustomMetricBo agentCustomMetricBo = this.agentCustomMetricMapper.map(customMetricMessage, header);
        if (agentCustomMetricBo == null) {
            return;
        }

        agentCustomMetricDispatchService.save(agentCustomMetricBo);
    }

}
