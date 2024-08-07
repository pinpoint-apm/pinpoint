/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc.retry;

import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

public class GrpcRetryFriendlyServerResponse<T> implements ServerResponse<T> {
    private final StreamObserver<T> responseObserver;

    public GrpcRetryFriendlyServerResponse(StreamObserver<T> responseObserver) {
        this.responseObserver = Objects.requireNonNull(responseObserver, "responseObserver");
    }

    @Override
    public void write(final T message) {
        Objects.requireNonNull(message, "message");
        if (message instanceof final PResult pResult) {
            if (!pResult.getSuccess()) {
                responseObserver.onError(Status.UNAVAILABLE.withDescription(pResult.getMessage()).asRuntimeException());
                return;
            }
        }

        responseObserver.onNext(message);
        responseObserver.onCompleted();
    }

    @Override
    public void finish() {
        responseObserver.onCompleted();
    }
}