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
import com.navercorp.pinpoint.collector.service.SqlUidMetaDataService;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
public class GrpcSqlUidMetaDataHandler implements RequestResponseHandler<PSqlUidMetaData, PResult> {
    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final SqlUidMetaDataService[] sqlUidMetaDataServices;

    public GrpcSqlUidMetaDataHandler(SqlUidMetaDataService[] sqlUidMetaDataServices) {
        this.sqlUidMetaDataServices = Objects.requireNonNull(sqlUidMetaDataServices, "sqlUidMetaDataServices");
        logger.info("SqlUidMetaDataServices {}", Arrays.toString(sqlUidMetaDataServices));
    }

    @Override
    public void handleRequest(ServerRequest<PSqlUidMetaData> serverRequest, ServerResponse<PResult> serverResponse) {
        final PSqlUidMetaData sqlUidMetaData = serverRequest.getData();
        final ServerHeader header = serverRequest.getHeader();
        PResult result = handleSqlUidMetaData(header, sqlUidMetaData);
        serverResponse.write(result);

    }

    private PResult handleSqlUidMetaData(ServerHeader header, PSqlUidMetaData sqlUidMetaData) {
        if (isDebug) {
            logger.debug("Handle PSqlUidMetaData={}", MessageFormatUtils.debugLog(sqlUidMetaData));
        }

        SqlUidMetaDataBo sqlUidMetaDataBo = mapSqlUidMetaDataBo(header, sqlUidMetaData);

        boolean result = true;
        for (SqlUidMetaDataService sqlUidMetaDataService : sqlUidMetaDataServices) {
            try {
                sqlUidMetaDataService.insert(sqlUidMetaDataBo);
            } catch (Throwable e) {
                // Avoid detailed error messages.
                logger.warn("Failed to handle sqlUidMetaData={}", MessageFormatUtils.debugLog(sqlUidMetaData), e);
                result = false;
            }
        }

        return newResult(result);
    }

    private static SqlUidMetaDataBo mapSqlUidMetaDataBo(ServerHeader agentInfo, PSqlUidMetaData sqlUidMetaData) {
        final String agentId = agentInfo.getAgentId();
        final long agentStartTime = agentInfo.getAgentStartTime();
        final String applicationName = agentInfo.getApplicationName();
        final byte[] sqlUid = sqlUidMetaData.getSqlUid().toByteArray();
        final String sql = sqlUidMetaData.getSql();

        return new SqlUidMetaDataBo(agentId, agentStartTime, applicationName, sqlUid, sql);
    }

    private static PResult newResult(boolean success) {
        if (success) {
            return PResults.SUCCESS;
        } else {
            return PResults.INTERNAL_SERVER_ERROR;
        }
    }
}