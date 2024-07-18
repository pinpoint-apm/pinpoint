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
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author youngjin.kim2
 */
public class EmptyCommandService extends ProfilerCommandServiceGrpc.ProfilerCommandServiceImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public StreamObserver<PCmdMessage> handleCommand(StreamObserver<PCmdRequest> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(PCmdMessage pCmdMessage) {
                logger.debug("handleCommand onNext:{}", pCmdMessage);
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                Metadata metadata = Status.trailersFromThrowable(throwable);
                logger.debug("handleCommand onError {} {}", status, metadata);
            }

            @Override
            public void onCompleted() {
                logger.debug("handleCommand onCompleted");
            }
        };
    }

    @Override
    public StreamObserver<PCmdMessage> handleCommandV2(StreamObserver<PCmdRequest> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(PCmdMessage pCmdMessage) {
                logger.debug("handleCommandV2 onNext:{}", pCmdMessage);
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                Metadata metadata = Status.trailersFromThrowable(throwable);
                logger.debug("handleCommandV2 onError {} {}", status, metadata);
            }

            @Override
            public void onCompleted() {
                logger.debug("handleCommandV2 onCompleted");
            }
        };
    }

}
