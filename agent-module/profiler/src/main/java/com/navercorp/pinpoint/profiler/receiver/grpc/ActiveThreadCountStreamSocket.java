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
import com.navercorp.pinpoint.grpc.stream.ClientCallContext;
import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.grpc.stream.StreamUtils;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.CallStreamObserver;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountStreamSocket implements GrpcProfilerStreamSocket<PCmdActiveThreadCountRes>,
        ClientResponseObserver<PCmdActiveThreadCountRes, Empty> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcStreamService grpcStreamService;

    private final int socketId;
    private final int streamObserverId;

    private int sequenceId = 0;

    private final ClientCallContext context = new ClientCallContext();
    private ClientCallStateStreamObserver<PCmdActiveThreadCountRes> requestStream;
    private volatile boolean closed = false;

    public ActiveThreadCountStreamSocket(int socketId, int streamObserverId,
                                         GrpcStreamService grpcStreamService) {
        this.socketId = socketId;
        this.streamObserverId = streamObserverId;
        this.grpcStreamService = Objects.requireNonNull(grpcStreamService, "grpcStreamService");
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<PCmdActiveThreadCountRes> requestStream) {
        this.requestStream = ClientCallStateStreamObserver.clientCall(requestStream, context);
    }

    public PCmdStreamResponse newHeader() {
        PCmdStreamResponse.Builder headerResponseBuilder = PCmdStreamResponse.newBuilder();
        headerResponseBuilder.setResponseId(streamObserverId);
        headerResponseBuilder.setSequenceId(getSequenceId());
        return headerResponseBuilder.build();
    }


    @Override
    public boolean send(PCmdActiveThreadCountRes activeThreadCount) {
        if (closed) {
            return false;
        }
        final CallStreamObserver<PCmdActiveThreadCountRes> request = this.requestStream;
        if (request.isReady()) {
            request.onNext(activeThreadCount);
            return true;
        } else {
            logger.info("Send fail. (ActiveThreadCount) client is not ready. socketId:{} streamObserverId:{}", socketId, streamObserverId);
        }
        return false;
    }

    private int getSequenceId() {
        return ++sequenceId;
    }

    @Override
    public void close() {
        close(null);
    }

    @Override
    public void close(@Nullable Throwable throwable) {
        if (closed) {
            return;
        }
        logger.warn("close", throwable);
        dispose();

        StreamUtils.onCompleted(requestStream, (th) -> logger.info("close", th));
    }

    public boolean isClosed() {
        return closed;
    }


    private void dispose() {
        this.closed = true;
        grpcStreamService.unregister(this);
    }

    @Override
    public void onNext(Empty empty) {
        logger.debug("onNext {}", empty);
    }

    @Override
    public void onError(Throwable throwable) {
        this.context.response().onErrorState();

        Status status = Status.fromThrowable(throwable);
        Metadata metadata = Status.trailersFromThrowable(throwable);
        logger.info("onError {}. {} {}", this, status, metadata);

        this.dispose();

        if (requestStream.isRun()) {
            StreamUtils.onCompleted(requestStream, (th) -> logger.info("onError", th));
        }
    }

    @Override
    public void onCompleted() {
        this.context.response().onCompleteState();

        logger.info("onCompleted {}", this);

        this.dispose();

        if (requestStream.isRun()) {
            StreamUtils.onCompleted(requestStream, (th) -> logger.info("onCompleted", th));
        }
    }

    @Override
    public String toString() {
        return "ActiveThreadCountStreamSocket{" +
                "socketId=" + socketId +
                ", streamObserverId=" + streamObserverId +
                '}';
    }
}
