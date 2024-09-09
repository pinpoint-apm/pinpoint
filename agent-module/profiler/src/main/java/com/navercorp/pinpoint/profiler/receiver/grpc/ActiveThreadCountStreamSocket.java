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

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountStreamSocket implements GrpcProfilerStreamSocket<PCmdActiveThreadCountRes, Empty> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcStreamService grpcStreamService;

    private final int streamObserverId;
    private int sequenceId = 0;

    private final PinpointClientResponseObserver<PCmdActiveThreadCountRes, Empty> clientResponseObserver;

    public ActiveThreadCountStreamSocket(int streamObserverId, GrpcStreamService grpcStreamService) {
        this.streamObserverId = streamObserverId;
        this.grpcStreamService = Objects.requireNonNull(grpcStreamService, "grpcStreamService");
        this.clientResponseObserver = new PinpointClientResponseObserver<>(this);
    }

    public PCmdStreamResponse newHeader() {
        PCmdStreamResponse.Builder headerResponseBuilder = PCmdStreamResponse.newBuilder();
        headerResponseBuilder.setResponseId(streamObserverId);
        headerResponseBuilder.setSequenceId(getSequenceId());
        return headerResponseBuilder.build();
    }

    @Override
    public void send(PCmdActiveThreadCountRes activeThreadCount) {
        if (clientResponseObserver.isReady()) {
            clientResponseObserver.sendRequest(activeThreadCount);
        } else {
            logger.info("Send fail. (ActiveThreadCount) client is not ready. streamObserverId:{}", streamObserverId);
        }
    }

    private int getSequenceId() {
        return ++sequenceId;
    }

    @Override
    public void close() {
        logger.info("close");
        close0(null);
    }

    @Override
    public void close(Throwable throwable) {
        logger.warn("close. message:{}", throwable.getMessage(), throwable);
        close0(throwable);
    }


    @Override
    public void disconnect() {
        logger.info("disconnect");
        close0(null);
    }

    @Override
    public void disconnect(Throwable throwable) {
        logger.info("disconnect. message:{}", throwable.getMessage(), throwable);
        close0(throwable);
    }

    private void close0(Throwable throwable) {
        clientResponseObserver.close(throwable);
        grpcStreamService.unregister(this);
    }

    @Override
    public ClientResponseObserver<PCmdActiveThreadCountRes, Empty> getResponseObserver() {
        return clientResponseObserver;
    }

}
