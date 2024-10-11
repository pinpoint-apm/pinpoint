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

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ResponseStreamObserver<ResT> implements StreamObserver<ResT> {

    private static final Logger LOGGER = LogManager.getLogger(ResponseStreamObserver.class);

    private final String responseName;

    public ResponseStreamObserver(String responseName) {
        this.responseName = Objects.requireNonNull(responseName, "responseName");
    }

    @Override
    public void onNext(ResT value) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{} onNext {}", responseName, value.getClass().getSimpleName());
        }
    }

    @Override
    public void onError(Throwable t) {
        Status status = Status.fromThrowable(t);
        Metadata metadata = Status.trailersFromThrowable(t);
        LOGGER.info("{} onError {} {}", responseName, status, metadata);
    }

    @Override
    public void onCompleted() {
        LOGGER.info("{} onCompleted", responseName);
    }

    public static <ResT> StreamObserver<ResT> responseStream(String responseName) {
        return new ResponseStreamObserver<>(responseName);
    }

}
