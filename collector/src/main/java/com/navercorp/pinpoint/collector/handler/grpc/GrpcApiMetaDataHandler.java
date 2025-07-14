/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.service.ApiMetaDataService;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.util.LineNumber;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcApiMetaDataHandler implements RequestResponseHandler<PApiMetaData, PResult> {

    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ApiMetaDataService apiMetaDataService;

    public GrpcApiMetaDataHandler(ApiMetaDataService apiMetaDataService) {
        this.apiMetaDataService = Objects.requireNonNull(apiMetaDataService, "apiMetaDataService");
    }

    @Override
    public void handleRequest(ServerRequest<PApiMetaData> serverRequest, ServerResponse<PResult> serverResponse) {
        final PApiMetaData apiMetaData = serverRequest.getData();
        final ServerHeader header = serverRequest.getHeader();
        PResult result = handleApiMetaData(header, apiMetaData);
        serverResponse.write(result);
    }

    PResult handleApiMetaData(ServerHeader header, final PApiMetaData apiMetaData) {
        if (isDebug) {
            logger.debug("Handle PApiMetaData={}", MessageFormatUtils.debugLog(apiMetaData));
        }

        try {
            final String agentId = header.getAgentId();
            final long agentStartTime = header.getAgentStartTime();
            final int line = LineNumber.defaultLineNumber(apiMetaData.getLine());

            final MethodTypeEnum type = MethodTypeEnum.defaultValueOf(apiMetaData.getType());

            final ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo.Builder(agentId, agentStartTime,
                    apiMetaData.getApiId(), line, type, apiMetaData.getApiInfo())
                    .setLocation(apiMetaData.getLocation())
                    .build();

            this.apiMetaDataService.insert(apiMetaDataBo);
            return PResults.SUCCESS;
        } catch (Exception e) {
            logger.warn("Failed to handle apiMetaData={}", MessageFormatUtils.debugLog(apiMetaData), e);
            // Avoid detailed error messages.
            return PResults.INTERNAL_SERVER_ERROR;
        }
    }
}