package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class PinpointThreadFactoryTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testCreateThreadFactory() throws Exception {
        final AtomicInteger test = new AtomicInteger(0);

        PinpointThreadFactory pinpoint = new PinpointThreadFactory("pinpoint");
        Thread thread = pinpoint.newThread(new Runnable() {
            @Override
            public void run() {
                test.getAndIncrement();
            }
        });
        thread.start();
        thread.join();
        Assert.assertEquals(test.get(), 1);
        String threadName = thread.getName();
        logger.info(threadName);
        Assert.assertTrue(threadName.startsWith("pinpoint("));
        Assert.assertTrue(threadName.endsWith(")"));

        Thread thread2 = pinpoint.newThread(new Runnable() {
            @Override
            public void run() {
            }
        });
        logger.info(thread2.getName());

    }
}
