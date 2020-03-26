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

import com.navercorp.pinpoint.collector.handler.SimpleAndRequestResponseHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.GrpcAgentInfoBoMapper;
import com.navercorp.pinpoint.collector.service.AgentInfoService;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 */
@Service
public class GrpcAgentInfoHandler implements SimpleAndRequestResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AgentInfoService agentInfoService;

    private final GrpcAgentInfoBoMapper agentInfoBoMapper;

    public GrpcAgentInfoHandler(AgentInfoService agentInfoService, GrpcAgentInfoBoMapper agentInfoBoMapper) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentInfoBoMapper = Objects.requireNonNull(agentInfoBoMapper, "agentInfoBoMapper");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof PAgentInfo) {
            handleAgentInfo((PAgentInfo) data);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (data instanceof PAgentInfo) {
            final PResult result = handleAgentInfo((PAgentInfo) data);
            serverResponse.write(result);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private PResult handleAgentInfo(PAgentInfo agentInfo) {
        if (isDebug) {
            logger.debug("Handle PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        try {
            // agent info
            final Header header = ServerContext.getAgentInfo();
            final AgentInfoBo agentInfoBo = this.agentInfoBoMapper.map(agentInfo, header);
            this.agentInfoService.insert(agentInfoBo);
            return PResult.newBuilder().setSuccess(true).build();
        } catch (Exception e) {
            logger.warn("Failed to handle. agentInfo={}", MessageFormatUtils.debugLog(agentInfo), e);
            // Avoid detailed error messages.
            return PResult.newBuilder().setSuccess(false).setMessage("Internal Server Error").build();
        }
    }
}