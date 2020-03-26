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
import com.navercorp.pinpoint.collector.service.SqlMetaDataService;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
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
 */
@Service
public class GrpcSqlMetaDataHandler implements RequestResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final SqlMetaDataService sqlMetaDataService;

    public GrpcSqlMetaDataHandler(SqlMetaDataService sqlMetaDataService) {
        this.sqlMetaDataService = Objects.requireNonNull(sqlMetaDataService, "sqlMetaDataService");
    }


    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (data instanceof PSqlMetaData) {
            Object result = handleSqlMetaData((PSqlMetaData) data);
            serverResponse.write(result);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private Object handleSqlMetaData(PSqlMetaData sqlMetaData) {
        if (isDebug) {
            logger.debug("Handle PSqlMetaData={}", MessageFormatUtils.debugLog(sqlMetaData));
        }

        try {
            final Header agentInfo = ServerContext.getAgentInfo();
            final String agentId = agentInfo.getAgentId();
            final long agentStartTime = agentInfo.getAgentStartTime();

            final SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo(agentId, agentStartTime, sqlMetaData.getSqlId());
            sqlMetaDataBo.setSql(sqlMetaData.getSql());
            sqlMetaDataService.insert(sqlMetaDataBo);
            return PResult.newBuilder().setSuccess(true).build();
        } catch (Exception e) {
            logger.warn("Failed to handle sqlMetaData={}", MessageFormatUtils.debugLog(sqlMetaData), e);
            // Avoid detailed error messages.
            return PResult.newBuilder().setSuccess(false).setMessage("Internal Server Error").build();
        }
    }
}