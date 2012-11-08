package com.profiler.context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

public class SpanTest {

    @Test
    public void span() {
        int testSize = 1;

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(testSize);
        Executor exec = Executors.newFixedThreadPool(testSize);

        for (int i = 0; i < testSize; i++) {
            exec.execute(new TraceTest(startLatch, endLatch));
        }

        startLatch.countDown();

        try {
            endLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private static final class TraceTest implements Runnable {
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;

        public TraceTest(CountDownLatch startLatch, CountDownLatch endLatch) {
            this.startLatch = startLatch;
            this.endLatch = endLatch;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Trace trace = new Trace();
//            trace.setTraceId(Trace.getNextTraceId());

            trace.recordMessage("msg:client send");
            trace.record(Annotation.ClientSend);

            trace.recordMessage("msg:server recv");
            trace.record(Annotation.ServerRecv);

            trace.recordMessage("msg:server send");
            trace.record(Annotation.ServerSend);

            trace.recordMessage("msg:client recv");
            trace.record(Annotation.ClientRecv);

            endLatch.countDown();
        }
    }
}
