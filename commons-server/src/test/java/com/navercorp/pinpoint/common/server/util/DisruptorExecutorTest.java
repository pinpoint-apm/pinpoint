package com.navercorp.pinpoint.common.server.util;

import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.navercorp.pinpoint.common.server.util.concurrent.DisruptorExecutors;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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
        ExecutorService executor = DisruptorExecutors.newSingleProducerExecutor(2, bufferSize, threadFactory);
    }


    @Test
    public void simpleTest() throws InterruptedException {
        int bufferSize = 10;
        int newBufferSize = DisruptorExecutors.nextPowerOfTwo(bufferSize);

        ExecutorService executor = DisruptorExecutors.newSingleProducerExecutor(1, newBufferSize, threadFactory, new TimeoutBlockingWaitStrategy(10000, TimeUnit.MILLISECONDS));

        CountDownLatch latch = new CountDownLatch(1);
        execute(newBufferSize, executor, latch);

        List<Runnable> runnableList = executor.shutdownNow();
        latch.countDown();

        Assert.assertEquals(newBufferSize - 1, runnableList.size());
        Assert.assertTrue(executor.isShutdown());
        //Assert.assertFalse(executor.isTerminated());
    }

    @Test
    public void simpleTest2() throws InterruptedException {
        int bufferSize = 10;
        int newBufferSize = DisruptorExecutors.nextPowerOfTwo(bufferSize);

        ExecutorService executor = DisruptorExecutors.newSingleProducerExecutor(1, newBufferSize, threadFactory, new TimeoutBlockingWaitStrategy(10000, TimeUnit.MILLISECONDS));

        CountDownLatch latch = new CountDownLatch(1);
        execute(newBufferSize, executor, latch);

        latch.countDown();
        executor.shutdown();

        Assert.assertTrue(executor.isShutdown());
        Assert.assertTrue(executor.isTerminated());
    }

    @Test
    public void rejectTest() throws Exception {
        int bufferSize = 10;
        int newBufferSize = DisruptorExecutors.nextPowerOfTwo(bufferSize);
        ExecutorService executor = DisruptorExecutors.newSingleProducerExecutor(2, newBufferSize, threadFactory, new TimeoutBlockingWaitStrategy(100, TimeUnit.MILLISECONDS));

        CountDownLatch latch = new CountDownLatch(1);
        try {
            execute(newBufferSize + 1, executor, latch);
            Assert.fail();
        } catch (Exception e) {
        } finally {
            latch.countDown();
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    private void execute(int newBufferSize, ExecutorService executor, CountDownLatch latch) {
        for (int i = 0; i < newBufferSize; i++) {
            executor.execute(new LatchAwaitRunnable(latch));
        }
    }

    class LatchAwaitRunnable implements Runnable {

        private final CountDownLatch latch;

        public LatchAwaitRunnable(CountDownLatch latch) {
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
