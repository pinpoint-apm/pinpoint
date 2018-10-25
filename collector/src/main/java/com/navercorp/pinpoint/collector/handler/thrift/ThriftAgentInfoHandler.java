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
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.mapper.thrift.AgentInfoBoMapper;
import com.navercorp.pinpoint.collector.service.AgentInfoService;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 * @author koo.taejin
 */
@Service
public class ThriftAgentInfoHandler implements SimpleHandler, RequestResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private AgentInfoBoMapper agentInfoBoMapper;

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle simple AgentInfo={}", data);
        }

        if (data instanceof TAgentInfo) {
            handleAgentInfo((TAgentInfo) data);
        } else if (data instanceof PAgentInfo) {
            handleAgentInfo((PAgentInfo) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }


    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle request AgentInfo={}", data);
        }

        if (data instanceof TAgentInfo) {
            final TBase<?, ?> tBase = handleAgentInfo((TAgentInfo) data);
            serverResponse.write(tBase);
        } else if (data instanceof PAgentInfo) {
            final TBase<?, ?> tBase = handleAgentInfo((PAgentInfo) data);
            serverResponse.write(tBase);
        } else {
            logger.warn("invalid serverRequest:{}", serverRequest);
        }
    }

    private TResult handleAgentInfo(TAgentInfo agentInfo) {
        try {
            // agent info
            final AgentInfoBo agentInfoBo = this.agentInfoBoMapper.map(agentInfo);
            this.agentInfoService.insert(agentInfoBo);
            return new TResult(true);
        } catch (Exception e) {
            logger.warn("AgentInfo handle error. Caused:{}", e.getMessage(), e);
            final TResult result = new TResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
    }

    private TResult handleAgentInfo(PAgentInfo agentInfo) {
        try {
            // agent info
            final AgentInfoBo agentInfoBo = this.agentInfoBoMapper.map(agentInfo);
            this.agentInfoService.insert(agentInfoBo);
            return new TResult(true);
        } catch (Exception e) {
            logger.warn("AgentInfo handle error. Caused:{}", e.getMessage(), e);
            final TResult result = new TResult(false);
            result.setMessage(e.getMessage());
            return result;
        }
    }
}