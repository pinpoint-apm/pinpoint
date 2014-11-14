package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
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