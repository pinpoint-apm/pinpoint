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
import com.navercorp.pinpoint.collector.service.SqlMetaDataService;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcSqlMetaDataHandler implements RequestResponseHandler<GeneratedMessageV3, GeneratedMessageV3> {
    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final SqlMetaDataService[] sqlMetaDataServices;

    public GrpcSqlMetaDataHandler(SqlMetaDataService[] sqlMetaDataServices) {
        this.sqlMetaDataServices = Objects.requireNonNull(sqlMetaDataServices, "sqlMetaDataServices");
        logger.info("SqlMetaDataServices {}", Arrays.toString(sqlMetaDataServices));
    }

    @Override
    public int type() {
        return DefaultTBaseLocator.SQLMETADATA;
    }

    @Override
    public void handleRequest(ServerRequest<GeneratedMessageV3> serverRequest, ServerResponse<GeneratedMessageV3> serverResponse) {
        final GeneratedMessageV3 data = serverRequest.getData();
        if (data instanceof PSqlMetaData sqlMetaData) {
            PResult result = handleSqlMetaData(sqlMetaData);
            serverResponse.write(result);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private PResult handleSqlMetaData(PSqlMetaData sqlMetaData) {
        if (isDebug) {
            logger.debug("Handle PSqlMetaData={}", MessageFormatUtils.debugLog(sqlMetaData));
        }

        final Header agentInfo = ServerContext.getAgentInfo();
        final SqlMetaDataBo sqlMetaDataBo = mapSqlMetaDataBo(agentInfo, sqlMetaData);

        boolean result = true;
        for (SqlMetaDataService sqlMetaDataService : sqlMetaDataServices) {
            try {
                sqlMetaDataService.insert(sqlMetaDataBo);
            } catch (Throwable e) {
                // Avoid detailed error messages.
                logger.warn("Failed to handle sqlMetaData={}", MessageFormatUtils.debugLog(sqlMetaData), e);
                result = false;
            }
        }

        return newResult(result);
    }

    private static SqlMetaDataBo mapSqlMetaDataBo(Header agentInfo, PSqlMetaData sqlMetaData) {
        final AgentId agentId = agentInfo.getAgentId();
        final long agentStartTime = agentInfo.getAgentStartTime();
        final int sqlId = sqlMetaData.getSqlId();
        final String sql = sqlMetaData.getSql();

        return new SqlMetaDataBo(AgentId.unwrap(agentId), agentStartTime, sqlId, sql);
    }

    private static SqlUidMetaDataBo mapSqlUidMetaDataBo(Header agentInfo, PSqlUidMetaData sqlUidMetaData) {
        final AgentId agentId = agentInfo.getAgentId();
        final long agentStartTime = agentInfo.getAgentStartTime();
        final String applicationName = agentInfo.getApplicationName();
        final byte[] sqlUid = sqlUidMetaData.getSqlUid().toByteArray();
        final String sql = sqlUidMetaData.getSql();

        return new SqlUidMetaDataBo(AgentId.unwrap(agentId), agentStartTime, applicationName, sqlUid, sql);
    }

    private static PResult newResult(boolean success) {
        final PResult.Builder builder = PResult.newBuilder();
        if (success) {
            builder.setSuccess(true);
        } else {
            builder.setSuccess(false).setMessage("Internal Server Error");
        }
        return builder.build();
    }
}