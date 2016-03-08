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

import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ThreadMXBeanUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testName() throws Exception {
        ThreadInfo[] threadInfos = ThreadMXBeanUtils.dumpAllThread();

        Assert.assertNotNull(threadInfos);
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

        Assert.assertFalse(ThreadMXBeanUtils.findThreadName(threadName));

        WaitingRunnable waiting = new WaitingRunnable();
        Thread thread = new Thread(waiting, threadName);
        thread.start();

        Assert.assertTrue(ThreadMXBeanUtils.findThreadName(threadName));

        waiting.stop();
        try {
            thread.join(2000);
        } catch (InterruptedException e) {
            Assert.fail();
        }

        Assert.assertFalse(ThreadMXBeanUtils.findThreadName(threadName));
    }

    private class WaitingRunnable implements Runnable {

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