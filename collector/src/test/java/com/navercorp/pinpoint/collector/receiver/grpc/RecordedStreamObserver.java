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

package com.navercorp.pinpoint.collector.receiver.grpc;

import io.grpc.stub.StreamObserver;

/**
 * @author Taejin Koo
 */
public class RecordedStreamObserver<T> implements StreamObserver<T> {

    private int requestCount;
    private T latestRequest;
    private Throwable latestThrowable;
    private boolean isCompleted = false;

    @Override
    public void onNext(T request) {
        requestCount++;
        this.latestRequest = request;
    }

    @Override
    public void onError(Throwable t) {
        this.latestThrowable = t;
    }

    @Override
    public void onCompleted() {
        this.isCompleted = true;
    }

    public T getLatestRequest() {
        return latestRequest;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public Throwable getLatestThrowable() {
        return latestThrowable;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

}
