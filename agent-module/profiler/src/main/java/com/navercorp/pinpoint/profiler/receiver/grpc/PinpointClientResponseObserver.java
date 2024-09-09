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

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class PinpointClientResponseObserver<ReqT, ResT> implements ClientResponseObserver<ReqT, ResT> {

    private final GrpcProfilerStreamSocket<ReqT, ResT> socket;

    private volatile ClientCallStreamObserver<ReqT> requestStream;

    public PinpointClientResponseObserver(GrpcProfilerStreamSocket<ReqT, ResT> socket) {
        this.socket = Objects.requireNonNull(socket, "socket");
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
        this.requestStream = requestStream;
    }

    @Override
    public void onNext(ResT res) {
        // do nothing
    }

    @Override
    public void onError(Throwable t) {
        socket.disconnect(t);
    }

    @Override
    public void onCompleted() {
        socket.disconnect();
    }

    public void sendRequest(ReqT value) {
        final ClientCallStreamObserver<ReqT> copy = this.requestStream;
        if (copy == null) {
            return;
        }
        copy.onNext(value);
    }

    public boolean isReady() {
        final ClientCallStreamObserver<ReqT> copy = this.requestStream;
        if (copy == null) {
            return false;
        }
        return copy.isReady();
    }


    public void close(Throwable throwable) {
        final ClientCallStreamObserver<ReqT> copy = requestStream;
        if (copy == null) {
            return;
        }

        if (throwable == null) {
            copy.onCompleted();
        } else {
            copy.onError(throwable);
        }
    }

}