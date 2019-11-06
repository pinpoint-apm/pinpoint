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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class QueueingStreamObserver<V> implements StreamObserver<V> {
    private final BlockingQueue<V> queue = new ArrayBlockingQueue<V>(1024);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public QueueingStreamObserver() {
    }


    @Override
    public void onNext(V value) {
        logger.debug("onNext value:{}", value);
        queue.add(value);
    }

    @Override
    public void onError(Throwable t) {
        logger.debug("onError:{}", Status.fromThrowable(t), t);
    }

    @Override
    public void onCompleted() {
        logger.debug("onCompleted");
    }

    public V getValue() {
        try {
            return queue.poll(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
};