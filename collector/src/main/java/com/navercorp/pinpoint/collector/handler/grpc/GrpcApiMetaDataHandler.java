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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.service.ApiMetaDataService;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.util.LineNumber;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcApiMetaDataHandler implements RequestResponseHandler<GeneratedMessageV3, GeneratedMessageV3> {

    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ApiMetaDataService apiMetaDataService;

    public GrpcApiMetaDataHandler(ApiMetaDataService apiMetaDataService) {
        this.apiMetaDataService = Objects.requireNonNull(apiMetaDataService, "apiMetaDataService");
    }

    @Override
    public int type() {
        return DefaultTBaseLocator.APIMETADATA;
    }

    @Override
    public void handleRequest(ServerRequest<GeneratedMessageV3> serverRequest, ServerResponse<GeneratedMessageV3> serverResponse) {
        final GeneratedMessageV3 data = serverRequest.getData();
        if (data instanceof PApiMetaData apiMetaData) {
            GeneratedMessageV3 result = handleApiMetaData(apiMetaData);
            serverResponse.write(result);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    PResult handleApiMetaData(final PApiMetaData apiMetaData) {
        if (isDebug) {
            logger.debug("Handle PApiMetaData={}", MessageFormatUtils.debugLog(apiMetaData));
        }

        try {
            final Header header = ServerContext.getAgentInfo();
            final AgentId agentId = header.getAgentId();
            final long agentStartTime = header.getAgentStartTime();
            final int line = LineNumber.defaultLineNumber(apiMetaData.getLine());

            final MethodTypeEnum type = MethodTypeEnum.defaultValueOf(apiMetaData.getType());

            final ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo.Builder(AgentId.unwrap(agentId), agentStartTime,
                    apiMetaData.getApiId(), line, type, apiMetaData.getApiInfo())
                    .setLocation(apiMetaData.getLocation())
                    .build();

            this.apiMetaDataService.insert(apiMetaDataBo);
            return PResult.newBuilder().setSuccess(true).build();
        } catch (Exception e) {
            logger.warn("Failed to handle apiMetaData={}", MessageFormatUtils.debugLog(apiMetaData), e);
            // Avoid detailed error messages.
            return PResult.newBuilder().setSuccess(false).setMessage("Internal Server Error").build();
        }
    }
}