package com.profiler.context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

import com.profiler.context.tracer.Tracer;

public class SpanTest {

	@Test
	public void span() {
		Trace.addTracer(new Tracer() {
			@Override
			public void record(Record record) {
				System.out.printf("[%s] Record=%s\n", Thread.currentThread().getId(), record);
			}
		});

		int testSize = 3;

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

			Trace.record("msg:client send");
			Trace.record(new Annotation.ClientSend());

			Trace.record("msg:server recv");
			Trace.record(new Annotation.ServerRecv());

			Trace.record("msg:server send");
			Trace.record(new Annotation.ServerSend());

			Trace.record("msg:client recg");
			Trace.record(new Annotation.ClientRecv());

			endLatch.countDown();
		}
	}
}
