package com.nhn.pinpoint.collector;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;

public class MultiThreadTester {

	private static final int THREAD_COUNT = 100;

	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch stopLatch = new CountDownLatch(1);

	private final StatisticsCache cache = new StatisticsCache();

	private class Worker implements Runnable {

		private int id;

		public Worker(int id) {
			this.id = id;
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
			}

			Random random = new Random();

			// DO JOB
			while (true) {
				if (id == 0) {
					List<Value> items;
					if (random.nextBoolean()) {
						items = cache.getItems();
					} else {
						items = cache.getAllItems();
					}
					for (Value v : items) {
						Assert.assertNotNull(v);
					}
				} else if (id == 1) {
					System.out.println(cache.size());
				} else {
					byte[] b = new byte[1];
					random.nextBytes(b);
					cache.add(b, b, 1L);
				}
			}
		}
	}

	private final ExecutorService executor = Executors.newFixedThreadPool(1024);

	@Test
	public void runTest() {
		for (int i = 0; i < THREAD_COUNT; i++) {
			executor.execute(new Worker(i));
		}
		startLatch.countDown();
		try {
			stopLatch.await();
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

		System.out.println("DONE");
	}

}
