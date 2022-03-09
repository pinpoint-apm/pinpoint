/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc.service.command;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.grpc.trace.PCmdSamplingRateResponse;
import io.grpc.stub.StreamObserver;

/**
 * @author yjqg6666
 */
public class SamplingRateService implements GrpcSimpleCommandService<PCmdSamplingRateResponse> {

    @Override
    public void handle(PinpointGrpcServer pinpointGrpcServer, PCmdSamplingRateResponse message, StreamObserver<Empty> responseObserver) {
        pinpointGrpcServer.handleMessage(message.getCommonResponse().getResponseId(), message);
    }

}
