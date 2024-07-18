/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ResponseStreamObserver<ReqT, ResT> implements ClientResponseObserver<ReqT, ResT> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final StreamEventListener<ReqT> listener;

    public ResponseStreamObserver(StreamEventListener<ReqT> listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    @Override
    public void beforeStart(final ClientCallStreamObserver<ReqT> requestStream) {
        logger.info("beforeStart {}", listener);
        requestStream.setOnReadyHandler(new Runnable() {
            private final AtomicLong isReadyCounter = new AtomicLong(0);

            @Override
            public void run() {
                final long isReadyCount = isReadyCounter.incrementAndGet();
                logger.info("onReadyHandler {} isReadyCount:{}", listener, isReadyCount);
                if (isReadyCount == 1) {
                    listener.start(requestStream);
                }
            }
        });
    }

    @Override
    public void onNext(ResT value) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} onNext:{}", listener, value);
        }
    }

    @Override
    public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        Metadata metadata = Status.trailersFromThrowable(t);

        logger.info("Failed to stream, name={}, {} {}", listener, status, metadata);

        listener.onError(t);
    }

    @Override
    public void onCompleted() {
        logger.info("{} onCompleted", listener);
        listener.onCompleted();
    }

    @Override
    public String toString() {
        return "ResponseStreamObserver{" +
                "name=" + listener +
                '}';
    }
}