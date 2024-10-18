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

import com.google.common.base.Suppliers;
import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.grpc.stream.StreamUtils;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ResponseStreamObserver<ReqT, ResT> implements ClientResponseObserver<ReqT, ResT> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private ClientCallStateStreamObserver<ReqT> requestStream;
    private final StreamEventListener<ReqT> listener;

    public ResponseStreamObserver(StreamEventListener<ReqT> listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    @Override
    public void beforeStart(final ClientCallStreamObserver<ReqT> stream) {
        this.requestStream = ClientCallStateStreamObserver.clientCall(stream);

        final Supplier<Void> startStream = Suppliers.memoize(() -> {
            logger.info("onReadyHandler startStream:{}", listener);
            listener.start(requestStream);
            return null;
        });

        logger.info("beforeStart {}", listener);
        this.requestStream.setOnReadyHandler(new Runnable() {
            @Override
            public void run() {
                logger.debug("onReadyHandler.run() {}", listener);
                startStream.get();
            }
        });
    }

    public ClientCallStateStreamObserver<ReqT> getRequestStream() {
        return requestStream;
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

        logger.info("onError Failed to stream, name={}, {} {}", listener, status, metadata);

        listener.onError(t);

        if (requestStream.isRun()) {
            StreamUtils.onCompleted(requestStream, (th) -> logger.info("ResponseStreamObserver.onError", th));
        }
    }

    @Override
    public void onCompleted() {
        logger.info("onCompleted {}", listener);
        listener.onCompleted();

        if (requestStream.isRun()) {
            StreamUtils.onCompleted(requestStream, (th) -> logger.info("ResponseStreamObserver.onCompleted", th));
        }
    }

    @Override
    public String toString() {
        return "ResponseStreamObserver{" +
                "name=" + listener +
                '}';
    }
}