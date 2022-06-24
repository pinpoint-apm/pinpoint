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

package com.navercorp.pinpoint.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ThreadMXBeanUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testName() {
        ThreadInfo[] threadInfos = ThreadMXBeanUtils.dumpAllThread();

        Assertions.assertNotNull(threadInfos);
        logger.trace("thread:{}", Arrays.toString(threadInfos));
    }

    @Test
    public void testOption() {
        final String option = ThreadMXBeanUtils.getOption();
        logger.debug("ThreadMXBean option:{}", option);
    }

    @Test
    public void testHasThreadName() {

        String threadName = "ThreadMXBeanUtils-test-thread";

        Assertions.assertFalse(ThreadMXBeanUtils.findThreadName(threadName));

        WaitingRunnable waiting = new WaitingRunnable();
        Thread thread = new Thread(waiting, threadName);
        thread.start();

        Assertions.assertTrue(ThreadMXBeanUtils.findThreadName(threadName));

        waiting.stop();
        try {
            thread.join(2000);
        } catch (InterruptedException e) {
            Assertions.fail();
        }

        Assertions.assertFalse(ThreadMXBeanUtils.findThreadName(threadName));
    }

    private static class WaitingRunnable implements Runnable {

        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() {
            latch.countDown();
        }

    }
}