package com.profiler.context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

import com.profiler.context.tracer.DefaultTracer;

public class SpanTest {

	@Test
	public void span() {
		Trace.addTracer(new DefaultTracer());

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

			Trace.setTraceId(Trace.getNextId());
			Trace.addTracer(new DefaultTracer());

			Trace.record("msg:client send");
			Trace.record(new Annotation.ClientSend());

			Trace.record("msg:server recv");
			Trace.record(new Annotation.ServerRecv());

			Trace.record("msg:server send");
			Trace.record(new Annotation.ServerSend());

			Trace.record("msg:client recv");
			Trace.record(new Annotation.ClientRecv());

			endLatch.countDown();
		}
	}
}
