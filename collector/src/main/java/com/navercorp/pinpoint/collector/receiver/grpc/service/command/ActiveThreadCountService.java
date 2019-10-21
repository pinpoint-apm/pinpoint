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

package com.navercorp.pinpoint.collector.receiver.grpc.service.command;

import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import com.navercorp.pinpoint.rpc.stream.StreamException;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountService implements GrpcStreamCommandService<PCmdActiveThreadCountRes, Empty> {

    @Override
    public StreamObserver<PCmdActiveThreadCountRes> handle(PinpointGrpcServer pinpointGrpcServer, StreamObserver<Empty> connectionObserver) {
        return new ActiveThreadCountStreamObserver(pinpointGrpcServer, connectionObserver);
    }

    public static class ActiveThreadCountStreamObserver implements StreamObserver<PCmdActiveThreadCountRes> {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final PinpointGrpcServer pinpointGrpcServer;
        private final ServerCallStreamObserver<Empty> connectionObserver;

        private volatile int streamChannelId = -1;

        public ActiveThreadCountStreamObserver(PinpointGrpcServer pinpointGrpcServer, StreamObserver<Empty> connectionObserver) {
            this.pinpointGrpcServer = Objects.requireNonNull(pinpointGrpcServer, "pinpointGrpcServer");
            if (connectionObserver instanceof ServerCallStreamObserver) {
                this.connectionObserver = (ServerCallStreamObserver<Empty>) connectionObserver;
            } else {
                throw new IllegalArgumentException("streamConnectionManagerObserver can not cast to ServerCallStreamObserver");
            }
        }

        @Override
        public void onNext(PCmdActiveThreadCountRes response) {
            if (streamChannelId == -1) {
                streamChannelId = response.getCommonStreamResponse().getResponseId();
            }

            PCmdStreamResponse headerResponse = response.getCommonStreamResponse();
            int sequenceId = headerResponse.getSequenceId();
            if (sequenceId == 1) {
                boolean success = pinpointGrpcServer.handleStreamCreateMessage(streamChannelId, connectionObserver);
                if (!success) {
                    connectionObserver.onError(new StatusException(Status.NOT_FOUND));
                    return;
                }
            }

            try {
                pinpointGrpcServer.handleStreamMessage(streamChannelId, response);
            } catch (StreamException e) {
                logger.warn("Failed to handle streamMessage. message:{}", e.getMessage(), e);
                connectionObserver.onError(new StatusException(Status.INTERNAL.withDescription(e.getMessage())));
            }
        }

        @Override
        public void onError(Throwable t) {
            logger.info("streamChannelId:{} onError:{}", streamChannelId, t.getMessage(), t);

            if (streamChannelId != -1) {
                pinpointGrpcServer.handleStreamDisconnected(streamChannelId, t);
            } else {
                logger.warn("streamChannelId is not initialized");
                connectionObserver.onError(t);
            }
        }

        @Override
        public void onCompleted() {
            logger.info("streamChannelId:{} onCompleted", streamChannelId);

            if (streamChannelId != -1) {
                pinpointGrpcServer.handleStreamDisconnected(streamChannelId);
            } else {
                logger.warn("streamChannelId is not initialized");
                connectionObserver.onCompleted();
            }
        }
    }
}
