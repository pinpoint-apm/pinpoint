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

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

/**
 * @author Taejin Koo
 */
public class PinpointClientResponseObserver<ReqT> implements ClientResponseObserver<ReqT, Empty> {

    private final GrpcProfilerStreamSocket pinpointGrpcProfilerStreamSocket;

    private volatile ClientCallStreamObserver<ReqT> requestStream;

    public PinpointClientResponseObserver(GrpcProfilerStreamSocket pinpointGrpcProfilerStreamSocket) {
        this.pinpointGrpcProfilerStreamSocket = Assert.requireNonNull(pinpointGrpcProfilerStreamSocket, "pinpointGrpcProfilerStreamSocket");
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
        this.requestStream = requestStream;
    }

    @Override
    public void onNext(Empty value) {
        // do nothing
    }

    @Override
    public void onError(Throwable t) {
        pinpointGrpcProfilerStreamSocket.disconnect(t);
    }

    @Override
    public void onCompleted() {
        pinpointGrpcProfilerStreamSocket.disconnect();
    }

    public boolean isReady() {
        return requestStream != null;
    }

    public ClientCallStreamObserver<ReqT> getRequestObserver() {
        return requestStream;
    }

}