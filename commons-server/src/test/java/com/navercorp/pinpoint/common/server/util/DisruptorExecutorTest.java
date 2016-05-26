package com.navercorp.pinpoint.common.server.util;

import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.navercorp.pinpoint.common.server.util.concurrent.DisruptorExecutor;
import com.navercorp.pinpoint.common.server.util.concurrent.DisruptorExecutors;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class DisruptorExecutorTest {

    PinpointThreadFactory threadFactory = new PinpointThreadFactory(getClass().getSimpleName());

    // bufferSize must be a power of 2
    @Test(expected = IllegalArgumentException.class)
    public void bufferSizeTest() throws Exception {

        int bufferSize = 10;

        DisruptorExecutor executor = DisruptorExecutors.newSingleProducerExecutor(2, bufferSize, threadFactory);
    }

    @Test
    public void rejectTest() throws Exception {
        int bufferSize = 10;
        int newBufferSize = DisruptorExecutors.nextPowerOfTwo(bufferSize);

        CountDownLatch latch = new CountDownLatch(1);

        DisruptorExecutor executor = DisruptorExecutors.newSingleProducerExecutor(2, newBufferSize, threadFactory, new TimeoutBlockingWaitStrategy(100, TimeUnit.MILLISECONDS));

        try {
            for (int i = 0; i < newBufferSize + 1; i++) {
                executor.execute(new SimpleRunnable(latch));
            }
            Assert.fail();
        } catch (Exception e) {
        } finally {
            latch.countDown();
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    class SimpleRunnable implements Runnable {

        private final CountDownLatch latch;

        public SimpleRunnable(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {

            }
        }

    }

}
