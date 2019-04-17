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
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcServerResponse;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.io.DefaultTBaseLocator;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

public class AgentService extends AgentGrpc.AgentImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DispatchHandler dispatchHandler;
    private final ServerRequestFactory serverRequestFactory = new ServerRequestFactory();

    public AgentService(DispatchHandler dispatchHandler) {
        this.dispatchHandler = Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
    }

    @Override
    public void requestAgentInfo(PAgentInfo agentInfo, StreamObserver<PResult> responseObserver) {
        logger.debug("Request AgentInfo {}", agentInfo);

        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.AGENT_INFO);
        final HeaderEntity headerEntity = new HeaderEntity(new HashMap<String, String>());
        Message<PAgentInfo> message = new DefaultMessage<PAgentInfo>(header, headerEntity, agentInfo);

        request(message, responseObserver);
    }

    @Override
    public void requestApiMetaData(PApiMetaData apiMetaData, StreamObserver<PResult> responseObserver) {
        logger.debug("Request ApiMetaData {}", apiMetaData);

        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.APIMETADATA);
        final HeaderEntity headerEntity = new HeaderEntity(new HashMap<String, String>());
        Message<PApiMetaData> message = new DefaultMessage<PApiMetaData>(header, headerEntity, apiMetaData);

        request(message, responseObserver);
    }

    @Override
    public void requestSqlMetaData(PSqlMetaData sqlMetaData, StreamObserver<PResult> responseObserver) {
        logger.debug("Request SqlMetaData {}", sqlMetaData);

        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.SQLMETADATA);
        final HeaderEntity headerEntity = new HeaderEntity(new HashMap<String, String>());
        Message<PSqlMetaData> message = new DefaultMessage<PSqlMetaData>(header, headerEntity, sqlMetaData);

        request(message, responseObserver);
    }

    @Override
    public void requestStringMetaData(PStringMetaData stringMetaData, StreamObserver<PResult> responseObserver) {
        logger.debug("Request StringMetaData {}", stringMetaData);

        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, DefaultTBaseLocator.STRINGMETADATA);
        final HeaderEntity headerEntity = new HeaderEntity(new HashMap<String, String>());
        Message<PStringMetaData> message = new DefaultMessage<PStringMetaData>(header, headerEntity, stringMetaData);

        request(message, responseObserver);
    }

    private void request(Message<?> message, StreamObserver<PResult> responseObserver) {
        ServerRequest<?> request;
        try {
            request = serverRequestFactory.newServerRequest(message);
        } catch (StatusException e) {
            logger.warn("serverRequest create fail Caused by:" + e.getMessage(), e);
            responseObserver.onError(e);
            return;
        }
        ServerResponse response = new GrpcServerResponse(responseObserver);
        this.dispatchHandler.dispatchRequestMessage(request, response);
    }
}