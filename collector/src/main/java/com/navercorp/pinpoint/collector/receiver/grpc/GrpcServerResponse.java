/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.util.ErrorStatus;
import com.navercorp.pinpoint.common.server.io.ServerResponse;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class GrpcServerResponse<T> implements ServerResponse<T> {

    protected final StreamObserver<T> responseObserver;

    public GrpcServerResponse(StreamObserver<T> responseObserver) {
        this.responseObserver = Objects.requireNonNull(responseObserver, "responseObserver");
    }

    @Override
    public void write(final T message) {
        Objects.requireNonNull(message, "message");

        responseObserver.onNext(message);
        responseObserver.onCompleted();
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof StatusException || throwable instanceof StatusRuntimeException) {
            responseObserver.onError(throwable);
        } else {
            // Avoid detailed exception
            responseObserver.onError(ErrorStatus.InternalBadRequest.asException());
        }
    }

    @Override
    public void finish() {
        responseObserver.onCompleted();
    }
}