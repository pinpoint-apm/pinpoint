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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ResponseStreamObserver<ReqT, RespT> implements ClientResponseObserver<ReqT, RespT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StreamId name;
    private final Reconnector reconnector;

    public ResponseStreamObserver(StreamId name, Reconnector reconnector) {
        this.name = Assert.requireNonNull(name, "name");
        this.reconnector = Assert.requireNonNull(reconnector, "reconnector");

    }

    @Override
    public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
        logger.info("beforeStart:{}", name);
        requestStream.setOnReadyHandler(new Runnable() {
            private final AtomicInteger counter = new AtomicInteger();
            @Override
            public void run() {
                logger.info("onReadyHandler:{} eventNumber:{}", name, counter.getAndIncrement());
                reconnector.reset();
            }
        });
    }

    @Override
    public void onNext(RespT value) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} onNext:{}", name, value);
        }
    }

    @Override
    public void onError(Throwable t) {
        final StatusError statusError = StatusErrors.throwable(t);
        if (statusError.isSimpleError()) {
            logger.info("Failed to stream, name={}, cause={}", name, statusError.getMessage());
        } else {
            logger.warn("Failed to stream, name={}, cause={}", name, statusError.getMessage(), statusError.getThrowable());
        }
        reconnector.reconnect();
    }

    @Override
    public void onCompleted() {
        logger.debug("{} onCompleted", name);
    }

    @Override
    public String toString() {
        return "ResponseStreamObserver{" +
                "name=" + name +
                '}';
    }
}