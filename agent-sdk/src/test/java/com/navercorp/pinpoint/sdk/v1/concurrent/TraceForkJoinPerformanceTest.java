package com.navercorp.pinpoint.sdk.v1.concurrent;

import com.navercorp.pinpoint.sdk.v1.concurrent.util.Counter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// it's slow
@Ignore
public class TraceForkJoinPerformanceTest {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

    public static class OriginFibonacciForkJoinTask extends RecursiveTask<Long> {

        private final long n;

        public OriginFibonacciForkJoinTask(long n) {
            this.n = n;
        }

        @Override
        protected Long compute() {
            Counter.add();
            if (n <= 1) {
                return n;
            }
            OriginFibonacciForkJoinTask f1 = new OriginFibonacciForkJoinTask(n - 1);
            OriginFibonacciForkJoinTask f2 = new OriginFibonacciForkJoinTask(n - 2);
            f1.fork();
            f2.fork();
            return f2.join() + f1.join();
        }
    }

    public static class WrappedFibonacciForkJoinTask extends RecursiveTask<Long> {

        private final long n;

        public WrappedFibonacciForkJoinTask(long n) {
            this.n = n;
        }

        @Override
        protected Long compute() {
            if (n <= 1) {
                return n;
            }
            ForkJoinTask<Long> f1 = TraceForkJoinTask.asyncEntry(new WrappedFibonacciForkJoinTask(n - 1));
            ForkJoinTask<Long> f2 = TraceForkJoinTask.asyncEntry(new WrappedFibonacciForkJoinTask(n - 2));
            f1.fork();
            f2.fork();
            return f2.join() + f1.join();
        }
    }

    private static class Result {
        public long n;
        public long callCnt;
        public long time;

        public Result(long n, long callCnt, long time) {
            this.n = n;
            this.callCnt = callCnt;
            this.time = time;
        }
    }

    private interface ForkJoinParam {
        ForkJoinTask<Long> get(int n);
    }

    @Test
    public void testReflectPerformance() throws ExecutionException, InterruptedException {
        // warm up
        testForkJoinTime(new ForkJoinParam() {
            @Override
            public ForkJoinTask<Long> get(int n) {
                return TraceForkJoinTask.asyncEntry(new WrappedFibonacciForkJoinTask(n));
            }
        });
        testForkJoinTime(new ForkJoinParam() {
            @Override
            public ForkJoinTask<Long> get(int n1) {
                return new OriginFibonacciForkJoinTask(n1);
            }
        });

        TimeUnit.SECONDS.sleep(2);
        // real start
        List<Result> wrappedResults = testForkJoinTime(new ForkJoinParam() {
            @Override
            public ForkJoinTask<Long> get(int n) {
                return TraceForkJoinTask.asyncEntry(new WrappedFibonacciForkJoinTask(n));
            }
        });
        List<Result> originResults = testForkJoinTime(new ForkJoinParam() {
            @Override
            public ForkJoinTask<Long> get(int n) {
                return new OriginFibonacciForkJoinTask(n);
            }
        });

        // compute performance loss
        for (int i = 0; i < originResults.size(); i++) {
            Result origin = originResults.get(i);
            Result wrapped = wrappedResults.get(i);
            double percent = origin.time == 0 ? 0 : (wrapped.time - origin.time) * 100.0 / origin.time;
            double each = wrapped.time * 1.0 / wrapped.callCnt;
            System.out.format("n=%s,direct call %s times(take %sms),reflect call %s times(take %sms), take more %.2f%%,avg every time %.6fms\n",
                    origin.n, origin.callCnt, origin.time, wrapped.callCnt, wrapped.time, percent, each);
        }
    }

    private List<Result> testForkJoinTime(ForkJoinParam param) throws InterruptedException, ExecutionException {
        int n = 30;
        List<Result> results = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Counter.reset();
            long start = System.currentTimeMillis();
            ForkJoinTask<Long> f = forkJoinPool.submit(param.get(i));
            f.get();
            long end = System.currentTimeMillis();
            results.add(new Result(i, Counter.get(), end - start));
        }
        return results;
    }
}
