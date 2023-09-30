/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector.receiver;

import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import io.grpc.stub.StreamObserver;

/**
 * @author youngjin.kim2
 */
public class EmptyCommandService extends ProfilerCommandServiceGrpc.ProfilerCommandServiceImplBase {

    @Override
    public StreamObserver<PCmdMessage> handleCommand(StreamObserver<PCmdRequest> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(PCmdMessage pCmdMessage) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    @Override
    public StreamObserver<PCmdMessage> handleCommandV2(StreamObserver<PCmdRequest> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(PCmdMessage pCmdMessage) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

}
