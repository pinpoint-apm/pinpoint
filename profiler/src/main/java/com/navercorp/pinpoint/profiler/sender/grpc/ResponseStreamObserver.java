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

/**
 * @author Woonduk Kang(emeroad)
 */
public class ResponseStreamObserver<ReqT, ResT> implements ClientResponseObserver<ReqT, ResT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StreamEventListener<ReqT> listener;

    public ResponseStreamObserver(StreamEventListener<ReqT> listener) {
        this.listener = Assert.requireNonNull(listener, "listener");
    }

    @Override
    public void beforeStart(final ClientCallStreamObserver<ReqT> requestStream) {
        logger.info("beforeStart {}", listener);
        requestStream.setOnReadyHandler(new Runnable() {
            @Override
            public void run() {
                listener.start(requestStream);
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
        final StatusError statusError = StatusErrors.throwable(t);
        if (statusError.isSimpleError()) {
            logger.info("Failed to stream, name={}, cause={}", listener, statusError.getMessage());
        } else {
            logger.warn("Failed to stream, name={}, cause={}", listener, statusError.getMessage(), statusError.getThrowable());
        }
        listener.onError(t);
    }

    @Override
    public void onCompleted() {
        logger.warn("{} onCompleted", listener);
        listener.onCompleted();

    }

    @Override
    public String toString() {
        return "ResponseStreamObserver{" +
                "name=" + listener +
                '}';
    }
}