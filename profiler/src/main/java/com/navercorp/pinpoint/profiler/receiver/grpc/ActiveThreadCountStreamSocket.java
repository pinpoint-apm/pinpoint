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
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import io.grpc.stub.ClientResponseObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountStreamSocket implements GrpcProfilerStreamSocket<PCmdActiveThreadCountRes.Builder> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GrpcStreamService grpcStreamService;

    private final int streamObserverId;
    private int sequenceId = 0;

    private final PinpointClientResponseObserver<PCmdActiveThreadCountRes> clientResponseObserver;

    public ActiveThreadCountStreamSocket(int streamObserverId, GrpcStreamService grpcStreamService) {
        this.streamObserverId = streamObserverId;
        this.grpcStreamService = Assert.requireNonNull(grpcStreamService, "grpcStreamService");
        this.clientResponseObserver = new PinpointClientResponseObserver<PCmdActiveThreadCountRes>(this);
    }

    @Override
    public void send(PCmdActiveThreadCountRes.Builder sendBuilder) {
        PCmdStreamResponse.Builder headerResponseBuilder = PCmdStreamResponse.newBuilder();
        headerResponseBuilder.setResponseId(streamObserverId);
        headerResponseBuilder.setSequenceId(getSequenceId());
        sendBuilder.setCommonStreamResponse(headerResponseBuilder.build());

        if (clientResponseObserver.isReady()) {
            clientResponseObserver.getRequestObserver().onNext(sendBuilder.build());
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
        if (clientResponseObserver.isReady()) {
            if (throwable == null) {
                clientResponseObserver.getRequestObserver().onCompleted();
            } else {
                clientResponseObserver.getRequestObserver().onError(throwable);
            }
        }
        grpcStreamService.unregister(this);
    }

    @Override
    public ClientResponseObserver getResponseObserver() {
        return clientResponseObserver;
    }

}
