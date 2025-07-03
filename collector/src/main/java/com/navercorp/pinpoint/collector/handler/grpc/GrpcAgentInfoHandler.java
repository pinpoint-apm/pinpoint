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

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.GrpcAgentInfoBoMapper;
import com.navercorp.pinpoint.collector.service.AgentInfoService;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.io.util.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 */
@Service
public class GrpcAgentInfoHandler implements RequestResponseHandler<PAgentInfo, PResult> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AgentInfoService agentInfoService;

    private final GrpcAgentInfoBoMapper agentInfoBoMapper;

    public GrpcAgentInfoHandler(AgentInfoService agentInfoService, GrpcAgentInfoBoMapper agentInfoBoMapper) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentInfoBoMapper = Objects.requireNonNull(agentInfoBoMapper, "agentInfoBoMapper");
    }

    @Override
    public MessageType type() {
        return MessageType.AGENT_INFO;
    }


    @Override
    public void handleRequest(ServerRequest<PAgentInfo> serverRequest, ServerResponse<PResult> serverResponse) {
        final PAgentInfo agentInfo = serverRequest.getData();
        final ServerHeader header = serverRequest.getHeader();

        final PResult result = handleAgentInfo(header, agentInfo);
        serverResponse.write(result);

    }

    private PResult handleAgentInfo(ServerHeader header, PAgentInfo agentInfo) {
        if (isDebug) {
            logger.debug("Handle PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        try {
            // agent info
            final AgentInfoBo agentInfoBo = this.agentInfoBoMapper.map(agentInfo, header);
            this.agentInfoService.insert(header.getServiceUid(), header.getApplicationUid(), agentInfoBo);
            return PResults.SUCCESS;
        } catch (Exception e) {
            logger.warn("Failed to handle. agentInfo={}", MessageFormatUtils.debugLog(agentInfo), e);
            // Avoid detailed error messages.
            return PResults.INTERNAL_SERVER_ERROR;
        }
    }
}