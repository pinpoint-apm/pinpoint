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

import com.navercorp.pinpoint.rpc.client.ConnectFuture.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class ConnectFutureTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void setResultTest() {
        ConnectFuture future = new ConnectFuture();
        future.setResult(Result.FAIL);
        future.setResult(Result.SUCCESS);
        Assertions.assertEquals(Result.FAIL, future.getResult());

        future = new ConnectFuture();
        future.setResult(Result.SUCCESS);
        future.setResult(Result.FAIL);
        Assertions.assertEquals(Result.SUCCESS, future.getResult());
    }

    @Test
    public void awaitTest1() throws InterruptedException {
        ConnectFuture future = new ConnectFuture();

        Thread thread = new Thread(new SetResultRunnable(future));
        thread.start();

        future.await();

        Assertions.assertEquals(Result.SUCCESS, future.getResult());
    }

    @Test
    public void awaitTest2() throws InterruptedException {
        ConnectFuture future = new ConnectFuture();

        Thread thread = new Thread(new SetResultRunnable(future));
        thread.start();

        future.awaitUninterruptibly();

        Assertions.assertEquals(Result.SUCCESS, future.getResult());
    }

    @Test
    public void awaitTest3() throws InterruptedException {
        ConnectFuture future = new ConnectFuture();

        Thread thread = new Thread(new SetResultRunnable(future));
        thread.start();

        future.await(TimeUnit.SECONDS.toMillis(1), TimeUnit.MILLISECONDS);

        Assertions.assertEquals(Result.SUCCESS, future.getResult());
    }

    @Test
    public void notCompletedTest() throws InterruptedException {
        ConnectFuture future = new ConnectFuture();
        long waitTime = 100;

        Thread thread = new Thread(new SetResultRunnable(future, waitTime));
        thread.start();

        boolean isReady = future.await(waitTime / 2, TimeUnit.MILLISECONDS);
        Assertions.assertFalse(isReady);
        Assertions.assertNull(future.getResult());

        isReady = future.await(waitTime, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(isReady);
        Assertions.assertEquals(Result.SUCCESS, future.getResult());
    }

    class SetResultRunnable implements Runnable {

        private final ConnectFuture future;
        private final long waitTime;

        public SetResultRunnable(ConnectFuture future) {
            this(future, -1L);
        }

        public SetResultRunnable(ConnectFuture future, long waitTime) {
            this.future = future;
            this.waitTime = waitTime;
        }

        @Override
        public void run() {
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            future.setResult(Result.SUCCESS);
        }

    }

}
