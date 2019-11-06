/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.receiver.ProfilerSimpleCommandService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class GrpcEchoService implements ProfilerSimpleCommandService<PCmdRequest> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub;

    public GrpcEchoService(ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub) {
        this.profilerCommandServiceStub = Assert.requireNonNull(profilerCommandServiceStub, "profilerCommandServiceStub");
    }

    @Override
    public void simpleCommandService(PCmdRequest request) {
        logger.info("simpleCommandService:{}", request);

        PCmdEcho commandEcho = request.getCommandEcho();

        PCmdEchoResponse.Builder responseBuilder = PCmdEchoResponse.newBuilder();
        responseBuilder.setMessage(commandEcho.getMessage());

        PCmdResponse commonResponse = PCmdResponse.newBuilder().setResponseId(request.getRequestId()).build();
        responseBuilder.setCommonResponse(commonResponse);

        profilerCommandServiceStub.commandEcho(responseBuilder.build(), EmptyStreamObserver.create());
    }

    @Override
    public short getCommandServiceCode() {
        return PCommandType.ECHO_VALUE;
    }

}
