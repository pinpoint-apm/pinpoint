/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author HyunGil Jeong
 */
public class TestWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CountDownLatch completeLatch;
    private final Callback callback;

    TestWorker(CountDownLatch completeLatch) {
        this(completeLatch, Callback.DO_NOTHING);
    }

    TestWorker(CountDownLatch completeLatch, Callback callback) {
        this.completeLatch = Objects.requireNonNull(completeLatch, "completeLatch");
        this.callback = Objects.requireNonNull(callback, "onWorkerRun");
    }

    @Override
    public void run() {
        try {
            callback.onRun();
            logger.debug("{} work complete.", Thread.currentThread().getName());
        } catch (Exception e) {
            callback.onError();
            logger.error("{} work complete with {}", Thread.currentThread().getName(), e);
        } finally {
            completeLatch.countDown();
        }
    }

    interface Callback {

        void onRun();

        void onError();

        Callback DO_NOTHING = new Callback() {
            @Override
            public void onRun() {
            }

            @Override
            public void onError() {
            }
        };
    }
}
