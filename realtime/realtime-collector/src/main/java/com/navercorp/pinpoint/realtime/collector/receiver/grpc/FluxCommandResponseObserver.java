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
package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.FluxSink;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public abstract class FluxCommandResponseObserver<T> implements StreamObserver<T> {

    private static final Logger logger = LogManager.getLogger(FluxCommandResponseObserver.class);

    private final ServerCallStreamObserver<Empty> connectionObserver;
    private final SinkRepository<FluxSink<T>> sinkRepository;

    private volatile long sinkId = -1;
    private volatile FluxSink<T> sink = null;

    public FluxCommandResponseObserver(
            ServerCallStreamObserver<Empty> connectionObserver,
            SinkRepository<FluxSink<T>> sinkRepository
    ) {
        this.connectionObserver = Objects.requireNonNull(connectionObserver, "connectionObserver");
        this.sinkRepository = Objects.requireNonNull(sinkRepository, "sinkRepository");
    }

    @Override
    public void onNext(T response) {
        boolean isHello = extractSequence(response) == 1;

        if (isHello) {
            connectionObserver.onNext(Empty.getDefaultInstance());
        }

        if (ensureSink(response) == null) {
            this.connectionObserver.onError(new StatusException(Status.INTERNAL.withDescription("sink not found")));
            return;
        }

        logger.debug("Realtime flux item received: sinkId = {}", sinkId);
        if (!isHello) {
            this.sink.next(response);
        }
    }

    private FluxSink<T> ensureSink(T response) {
        if (this.sinkId == -1 || this.sink == null) {
            return initSink(response);
        }
        return this.sink;
    }

    private FluxSink<T> initSink(T response) {
        this.sinkId = this.extractSinkId(response);
        this.sink = this.sinkRepository.get(this.sinkId);
        if (this.sink == null) {
            logger.warn("Failed to handle realtime flux item: sink {} not found", this.sinkId);
            return null;
        }
        return this.sink;
    }

    @Override
    public void onError(Throwable t) {
        if (t.getMessage().startsWith("CANCELLED")) {
            logger.info("Stream cancelled: sinkId = {}", sinkId);
        } else {
            logger.warn("Stream error: sinkId = {}, message = {}", sinkId, t.getMessage(), t);
        }

        this.connectionObserver.onCompleted();

        if (this.sink != null) {
            this.sink.error(t);
        } else {
            logger.warn("Failed to emit error: sink not found. the error may have occurred before the first message");
        }
    }

    @Override
    public void onCompleted() {
        logger.info("Completed stream: sinkId = {}", this.sinkId);

        this.connectionObserver.onCompleted();

        if (this.sink != null) {
            this.sink.complete();
        } else {
            logger.warn("Failed to emit complete: sink not found");
        }
    }

    protected abstract long extractSinkId(T response);

    protected abstract int extractSequence(T response);

}