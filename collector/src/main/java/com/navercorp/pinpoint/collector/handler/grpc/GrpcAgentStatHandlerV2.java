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

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author jaehong.kim
 */
@Service
public class GrpcAgentStatHandlerV2 implements SimpleHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final GrpcAgentStatMapper agentStatMapper;

    private final GrpcAgentStatBatchMapper agentStatBatchMapper;

    private final List<AgentStatService> agentStatServiceList;

    public GrpcAgentStatHandlerV2(GrpcAgentStatMapper agentStatMapper,
                                  GrpcAgentStatBatchMapper agentStatBatchMapper,
                                  Optional<List<AgentStatService>> agentStatServiceList) {
        this.agentStatMapper = Objects.requireNonNull(agentStatMapper, "agentStatMapper");
        this.agentStatBatchMapper = Objects.requireNonNull(agentStatBatchMapper, "agentStatBatchMapper");
        this.agentStatServiceList = Objects.requireNonNull(agentStatServiceList, "agentStatServiceList2").orElse(Collections.emptyList());
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof PAgentStat) {
            handleAgentStat((PAgentStat) data);
        } else if (data instanceof PAgentStatBatch) {
            handleAgentStatBatch((PAgentStatBatch) data);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private void handleAgentStat(PAgentStat agentStat) {
        if (isDebug) {
            logger.debug("Handle PAgentStat={}", MessageFormatUtils.debugLog(agentStat));
        }

        final AgentStatBo agentStatBo = this.agentStatMapper.map(agentStat);
        if (agentStatBo == null) {
            return;
        }

        for (AgentStatService agentStatService : agentStatServiceList) {
            try {
                agentStatService.save(agentStatBo);
            } catch (Exception e) {
                logger.warn("Failed to handle service={}, AgentStat={}", agentStatService, MessageFormatUtils.debugLog(agentStat), e);
            }
        }
    }

    private void handleAgentStatBatch(PAgentStatBatch agentStatBatch) {
        if (isDebug) {
            logger.debug("Handle PAgentStatBatch={}", MessageFormatUtils.debugLog(agentStatBatch));
        }

        Header header = ServerContext.getAgentInfo();
        final AgentStatBo agentStatBo = this.agentStatBatchMapper.map(agentStatBatch, header);
        if (agentStatBo == null) {
            return;
        }

        for (AgentStatService agentStatService : agentStatServiceList) {
            try {
                agentStatService.save(agentStatBo);
            } catch (Exception e) {
                logger.warn("Failed to handle service={}, AgentStatBatch={}", agentStatService, MessageFormatUtils.debugLog(agentStatBatch), e);
            }
        }
    }
}