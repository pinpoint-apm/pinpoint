/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class ConnectFuture {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final AtomicReferenceFieldUpdater<ConnectFuture, Result> FIELD_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ConnectFuture.class, Result.class, "result");

    private final CountDownLatch latch;
    private volatile Result result;

    public enum Result {
        SUCCESS, FAIL
    }

    public ConnectFuture() {
        this.latch = new CountDownLatch(1);
    }

    public Result getResult() {
        return this.result;
    }
    
    void setResult(Result connectResult) {
        final Result result = this.result;
        if (result == null) {
            if (FIELD_UPDATER.compareAndSet(this, null, connectResult)) {
                latch.countDown();
            }
        }
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return latch.await(timeout, timeUnit);
    }

    public void awaitUninterruptibly() {
        while (true) {
            try {
                await();
                return;
            } catch (InterruptedException e) {
                logger.debug(e.getMessage(), e);
            }
        }
    }

}
