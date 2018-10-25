/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler.thrift;

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.service.SqlMetaDataService;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class ThriftSqlMetaDataHandler implements RequestResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SqlMetaDataService sqlMetaDataService;

    public ThriftSqlMetaDataHandler() {
    }

    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle request data=={}", data);
        }

        if (data instanceof TSqlMetaData) {
            Object result = handleSqlMetaData((TSqlMetaData) data);
            serverResponse.write(result);
        } else if (data instanceof PSqlMetaData) {
            Object result = handleSqlMetaData((PSqlMetaData) data);
            serverResponse.write(result);
        } else {
            logger.warn("invalid serverRequest:{}", serverRequest);
        }
    }

    private Object handleSqlMetaData(TSqlMetaData sqlMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle TSqlMetaData={}", sqlMetaData);
        }

        try {
            final SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo(sqlMetaData.getAgentId(), sqlMetaData.getAgentStartTime(), sqlMetaData.getSqlId());
            sqlMetaDataBo.setSql(sqlMetaData.getSql());
            sqlMetaDataService.insert(sqlMetaDataBo);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            final TResult result = new TResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
        return new TResult(true);
    }

    private Object handleSqlMetaData(PSqlMetaData sqlMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PSqlMetaData={}", sqlMetaData);
        }
        return PResult.newBuilder().setSuccess(true).build();
    }
}