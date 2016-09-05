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

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(1);
        execute(newBufferSize, executor, startLatch, endLatch);
        startLatch.await();

        List<Runnable> runnableList = executor.shutdownNow();
        endLatch.countDown();

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
        execute(newBufferSize, executor, null, latch);
    }

    private void execute(int newBufferSize, ExecutorService executor, CountDownLatch startLatch, CountDownLatch endLatch) {
        for (int i = 0; i < newBufferSize; i++) {
            executor.execute(new LatchAwaitRunnable(startLatch, endLatch));
        }
    }

    @Test
    public void exceptionTest() throws InterruptedException {
        int bufferSize = 10;
        int newBufferSize = DisruptorExecutors.nextPowerOfTwo(bufferSize);

        ExecutorService executor = DisruptorExecutors.newSingleProducerExecutor(1, newBufferSize, threadFactory, new TimeoutBlockingWaitStrategy(10000, TimeUnit.MILLISECONDS));

        int executeCount = 5;
        CountDownLatch latch = new CountDownLatch(executeCount);
        try {
            for (int i = 0; i < executeCount; i++) {
                executor.execute(new ThrowExceptionRunnable(latch));
            }

            boolean await = latch.await(1000, TimeUnit.MILLISECONDS);
            Assert.assertTrue(await);
        } finally {
            executor.shutdown();
        }
    }

    class LatchAwaitRunnable implements Runnable {

        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;

        public LatchAwaitRunnable(CountDownLatch startLatch, CountDownLatch endLatch) {
            this.startLatch = startLatch;
            this.endLatch = endLatch;
        }

        @Override
        public void run() {
            if (startLatch != null) {
                startLatch.countDown();
            }

            try {
                endLatch.await();
            } catch (InterruptedException e) {
            }
        }
    }

    class ThrowExceptionRunnable implements Runnable {

        private final CountDownLatch latch;

        public ThrowExceptionRunnable(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {

            try {
                throw new RuntimeException();
            } finally {
                latch.countDown();
            }
        }

    }

}
