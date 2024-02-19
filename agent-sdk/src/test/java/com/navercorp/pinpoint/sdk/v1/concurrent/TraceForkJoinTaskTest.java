package com.navercorp.pinpoint.sdk.v1.concurrent;

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TraceForkJoinTaskTest {

    private final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

    private static class DirectFibonacciForkJoin extends ForkJoinTask<Long> {

        private final long n;
        private long result;

        private DirectFibonacciForkJoin(long n) {
            this.n = n;
        }

        @Override
        public Long getRawResult() {
            return result;
        }

        @Override
        protected void setRawResult(Long value) {
            result = value;
        }

        @Override
        protected boolean exec() {
            if (n <= 1) {
                result = n;
                return true;
            }
            TraceForkJoinTask<Long> f1 = new TraceForkJoinTask<>(new DirectFibonacciForkJoin(n - 1));
            TraceForkJoinTask<Long> f2 = new TraceForkJoinTask<>(new DirectFibonacciForkJoin(n - 2));
            f1.fork();
            f2.fork();
            result = f1.join() + f2.join();
            return true;
        }
    }

    private static class RecursiveFibonacciTask extends RecursiveTask<Long> {

        private final long n;

        private RecursiveFibonacciTask(long n) {
            this.n = n;
        }

        @Override
        protected Long compute() {
            if (n <= 1) {
                return n;
            }
            ForkJoinTask<Long> f1 = TraceForkJoinTask.asyncEntry(new RecursiveFibonacciTask(n - 1));
            ForkJoinTask<Long> f2 = TraceForkJoinTask.asyncEntry(new RecursiveFibonacciTask(n - 2));
            f1.fork();
            f2.fork();
            return f1.join() + f2.join();
        }
    }

    private static class RecursiveActionTask extends RecursiveAction {
        private final long[] array;
        private final int lo, hi;

        RecursiveActionTask(long[] array, int lo, int hi) {
            this.array = array;
            this.lo = lo;
            this.hi = hi;
        }

        protected void compute() {
            int THRESHOLD = 2;
            if (hi - lo < THRESHOLD) {
                for (int i = lo; i < hi; ++i) array[i]++;
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new TraceForkJoinTask<>(new RecursiveActionTask(array, lo, mid)),
                        new TraceForkJoinTask<>(new RecursiveActionTask(array, mid, hi)));
            }
        }
    }

    @Test
    public void testTraceForkJoin() throws ExecutionException, InterruptedException {
        ForkJoinTask<Long> f = pool.submit(new TraceForkJoinTask<>(new DirectFibonacciForkJoin(10)));
        assertEquals(55, f.get().longValue());

        f = pool.submit(new TraceForkJoinTask<>(new RecursiveFibonacciTask(10)));
        assertEquals(55, f.get().longValue());

        long[] arr = {1, 2, 3, 4, 5, 6, 7, 8};
        long[] resArr = {2, 3, 4, 5, 6, 7, 8, 9};
        ForkJoinTask<Void> f1 = pool.submit(new TraceForkJoinTask<>(new RecursiveActionTask(arr, 0, arr.length)));
        f1.get();
        assertArrayEquals(resArr, arr);
    }
}