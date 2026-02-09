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
import com.navercorp.pinpoint.collector.service.SqlMetaDataService;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.common.server.io.ServerResponse;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcSqlMetaDataHandler implements RequestResponseHandler<PSqlMetaData, PResult> {
    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final SqlMetaDataService[] sqlMetaDataServices;

    public GrpcSqlMetaDataHandler(SqlMetaDataService[] sqlMetaDataServices) {
        this.sqlMetaDataServices = Objects.requireNonNull(sqlMetaDataServices, "sqlMetaDataServices");
        logger.info("SqlMetaDataServices {}", Arrays.toString(sqlMetaDataServices));
    }

    @Override
    public void handleRequest(ServerRequest<PSqlMetaData> serverRequest, ServerResponse<PResult> serverResponse) {
        final PSqlMetaData sqlMetaData = serverRequest.getData();
        PResult result = handleSqlMetaData(serverRequest.getHeader(), sqlMetaData);
        serverResponse.write(result);
    }

    private PResult handleSqlMetaData(ServerHeader header, PSqlMetaData sqlMetaData) {
        if (isDebug) {
            logger.debug("Handle PSqlMetaData={}", MessageFormatUtils.debugLog(sqlMetaData));
        }

        final SqlMetaDataBo sqlMetaDataBo = mapSqlMetaDataBo(header, sqlMetaData);

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

    private static SqlMetaDataBo mapSqlMetaDataBo(ServerHeader agentInfo, PSqlMetaData sqlMetaData) {
        final String agentId = agentInfo.getAgentId();
        final long agentStartTime = agentInfo.getAgentStartTime();
        final int sqlId = sqlMetaData.getSqlId();
        final String sql = sqlMetaData.getSql();

        return new SqlMetaDataBo(agentId, agentStartTime, sqlId, sql);
    }

    private static PResult newResult(boolean success) {
        if (success) {
            return PResults.SUCCESS;
        } else {
            return PResults.INTERNAL_SERVER_ERROR;
        }
    }
}