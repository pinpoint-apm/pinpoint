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
import com.navercorp.pinpoint.collector.service.StringMetaDataService;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
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
public class GrpcStringMetaDataHandler implements RequestResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StringMetaDataService stringMetaDataService;

    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle request data={}", data);
        }

        if (data instanceof PStringMetaData) {
            Object result = handleStringMetaData((PStringMetaData) data);
            serverResponse.write(result);
        } else {
            logger.warn("invalid serverRequest:{}", serverRequest);
            return;
        }
    }

    private Object handleStringMetaData(final PStringMetaData stringMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PStringMetaData={}", stringMetaData);
        }

        try {
            final AgentHeaderFactory.Header agentInfo = ServerContext.getAgentInfo();
            final String agentId = agentInfo.getAgentId();
            final long agentStartTime = agentInfo.getAgentStartTime();

            final StringMetaDataBo stringMetaDataBo = new StringMetaDataBo(agentId, agentStartTime, stringMetaData.getStringId());
            stringMetaDataBo.setStringValue(stringMetaData.getStringValue());
            stringMetaDataService.insert(stringMetaDataBo);
        } catch (Exception e) {
            logger.warn("{} handler error. Caused:{}", this.getClass(), e.getMessage(), e);
            return PResult.newBuilder().setSuccess(false).setMessage(e.getMessage()).build();
        }
        return PResult.newBuilder().setSuccess(true).build();
    }
}