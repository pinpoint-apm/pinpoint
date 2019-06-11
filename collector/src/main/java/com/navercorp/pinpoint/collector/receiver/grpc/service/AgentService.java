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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * @author jaehong.kim
 */
public class AgentService extends AgentGrpc.AgentImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final SimpleRequestHandlerAdaptor<PResult> simpleRequestHandlerAdaptor;

    public AgentService(DispatchHandler dispatchHandler) {
        this.simpleRequestHandlerAdaptor = new SimpleRequestHandlerAdaptor<PResult>(this.getClass().getName(), dispatchHandler);
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        if (isDebug) {
            logger.debug("Request PAgentInfo={}", MessageFormatUtils.debugLog(agentInfo));
        }

        Message<PAgentInfo> message = newMessage(agentInfo, DefaultTBaseLocator.AGENT_INFO);

        simpleRequestHandlerAdaptor.request(message, responseObserver);
    }


    private <T> Message<T> newMessage(T requestData, short type) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type);
        final HeaderEntity headerEntity = new HeaderEntity(Collections.emptyMap());
        return new DefaultMessage<T>(header, headerEntity, requestData);
    }

}