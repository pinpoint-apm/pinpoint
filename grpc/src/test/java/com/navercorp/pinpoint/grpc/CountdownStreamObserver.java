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

package com.navercorp.pinpoint.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CountdownStreamObserver implements StreamObserver<Empty> {
    private final CountDownLatch latch;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CountdownStreamObserver() {
        this(1);
    }

    public CountdownStreamObserver(int count) {
        this.latch = new CountDownLatch(count);
    }

    @Override
    public void onNext(Empty value) {
        latch.countDown();
        logger.debug("onNext Empty:{}", value);
    }

    @Override
    public void onError(Throwable t) {
        logger.debug("onError:{}", Status.fromThrowable(t), t);
    }

    @Override
    public void onCompleted() {
        logger.debug("onCompleted");
    }

    public boolean awaitLatch() {
        try {
            return this.latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
};