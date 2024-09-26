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
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.realtime.collector.sink.ActiveThreadCountPublisher;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public abstract class ActiveThreadCountResponseStreamObserver implements StreamObserver<PCmdActiveThreadCountRes> {

    private static final Logger logger = LogManager.getLogger(ActiveThreadCountResponseStreamObserver.class);

    private final ServerCallStreamObserver<Empty> serverCallStreamObserver;
    private final SinkRepository<ActiveThreadCountPublisher> sinkRepository;

    private volatile long sinkId = -1;
    private volatile ActiveThreadCountPublisher publisher = null;

    public ActiveThreadCountResponseStreamObserver(ServerCallStreamObserver<Empty> serverCallStreamObserver, SinkRepository<ActiveThreadCountPublisher> sinkRepository) {
        this.serverCallStreamObserver = Objects.requireNonNull(serverCallStreamObserver, "serverCallStreamObserver");
        this.sinkRepository = Objects.requireNonNull(sinkRepository, "sinkRepository");
    }

    @Override
    public void onNext(PCmdActiveThreadCountRes response) {
        boolean isHello = extractSequence(response) == 1;

        if (isHello) {
            serverCallStreamObserver.onNext(Empty.getDefaultInstance());
        }

        final ActiveThreadCountPublisher publisher = ensureSink(response);
        if (publisher == null) {
            this.serverCallStreamObserver.onError(new StatusException(Status.INTERNAL.withDescription("sink not found")));
            return;
        }

        logger.debug("Realtime flux item received: sinkId = {}", sinkId);
        if (!isHello) {
            publisher.publish(response);
        }
    }

    private ActiveThreadCountPublisher ensureSink(PCmdActiveThreadCountRes response) {
        if (this.sinkId == -1 || publisher == null) {
            return initSink(response);
        }
        return this.publisher;
    }

    private ActiveThreadCountPublisher initSink(PCmdActiveThreadCountRes response) {
        this.sinkId = this.extractSinkId(response);
        this.publisher = this.sinkRepository.get(sinkId);
        if (this.publisher == null) {
            logger.warn("Failed to handle realtime flux item: sink {} not found", this.sinkId);
            return null;
        } else {
            publisher.setStreamObserver(this.serverCallStreamObserver);
        }
        return publisher;
    }

    @Override
    public void onError(Throwable t) {
        final Status status = Status.fromThrowable(t);
        if (Status.CANCELLED == status) {
            logger.info("Stream cancelled: sinkId = {} {}", sinkId, status);
        } else {
            logger.warn("Stream error: sinkId = {}, {}", sinkId, status);
        }

        this.serverCallStreamObserver.onCompleted();

        if (this.publisher != null) {
            this.publisher.error(t);
        } else {
            logger.warn("Failed to emit error: sink not found. the error may have occurred before the first message");
        }
    }

    @Override
    public void onCompleted() {
        logger.info("Completed stream: sinkId = {}", this.sinkId);

        this.serverCallStreamObserver.onCompleted();

        if (this.publisher != null) {
            this.publisher.complete();
        } else {
            logger.warn("Failed to emit complete: sink not found");
        }
    }

    protected abstract long extractSinkId(PCmdActiveThreadCountRes response);

    protected abstract int extractSequence(PCmdActiveThreadCountRes response);

}