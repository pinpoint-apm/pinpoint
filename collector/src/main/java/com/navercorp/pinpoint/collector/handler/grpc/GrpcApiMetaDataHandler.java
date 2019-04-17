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
import com.navercorp.pinpoint.collector.service.ApiMetaDataService;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class GrpcApiMetaDataHandler implements RequestResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApiMetaDataService apiMetaDataService;

    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle request data={}", data);
        }

        if (data instanceof PApiMetaData) {
            Object result = handleApiMetaData((PApiMetaData) data);
            serverResponse.write(result);
        } else {
            logger.warn("invalid serverRequest:{}", serverRequest);
        }
    }

    private Object handleApiMetaData(final PApiMetaData apiMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PApiMetaData={}", apiMetaData);
        }

        try {
            final AgentHeaderFactory.Header header = ServerContext.getAgentInfo();
            final String agentId = header.getAgentId();
            final long agentStartTime = header.getAgentStartTime();

            final ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo(agentId, agentStartTime, apiMetaData.getApiId());
            apiMetaDataBo.setApiInfo(apiMetaData.getApiInfo());

            final int line = apiMetaData.getLine();
            if (line != -1) {
                apiMetaDataBo.setLineNumber(line);
            }

            final int type = apiMetaData.getType();
            if (type != -1) {
                apiMetaDataBo.setMethodTypeEnum(MethodTypeEnum.valueOf(type));
            }

            this.apiMetaDataService.insert(apiMetaDataBo);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            return PResult.newBuilder().setSuccess(false).setMessage(e.getMessage()).build();
        }
        return PResult.newBuilder().setSuccess(true).build();
    }
}