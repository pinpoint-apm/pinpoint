package com.nhn.pinpoint.collector;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.Increment;
import org.junit.Test;

import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;

public class MultiThreadTester {

	private static final int TEST_COUNT = 10000000;
	private static final int THREAD_COUNT = 256;

	private static final byte[] ROW_KEY = "R".getBytes();
	private static final byte[] COLUMN_NAME = "C".getBytes();

	private ExecutorService executor;
	private AtomicLong resultValue;
	private CountDownLatch startLatch;
	private CountDownLatch stopLatch;
	private StatisticsCache cache;

	public void initialize() {
		if (executor != null) {
			executor.shutdownNow();
		}

		executor = Executors.newFixedThreadPool(THREAD_COUNT, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread t = new Thread(runnable);
				t.setName("UnitTest");
				t.setDaemon(true);
				return t;
			}
		});

		resultValue = new AtomicLong(0);

		startLatch = new CountDownLatch(1);
		stopLatch = new CountDownLatch(THREAD_COUNT);
		cache = new StatisticsCache(100, new StatisticsCache.FlushHandler() {
			@Override
			public void handleValue(Value value) {
				resultValue.addAndGet(value.getLongValue());
			}

			@Override
			public void handleValue(Increment increment) {
				Map<byte[], NavigableMap<byte[], Long>> familyMap = increment.getFamilyMap();
				for (Entry<byte[], NavigableMap<byte[], Long>> entry : familyMap.entrySet()) {
					NavigableMap<byte[], Long> innerMap = entry.getValue();
					for (Entry<byte[], Long> innerEntry : innerMap.entrySet()) {
						resultValue.addAndGet(innerEntry.getValue());
					}
				}
			}
		});

	}

	private class Worker implements Runnable {
		private int count = 0;

		@Override
		public void run() {
			try {
				startLatch.await();

				Random random = new Random();

				while (true) {
					if (count >= TEST_COUNT) {
						break;
					}

					byte[] b = new byte[1];
					random.nextBytes(b);
					cache.add(b, b, 1L);
					count++;
				}
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			} finally {
				cache.flush();
				stopLatch.countDown();
			}
		}
	}

	public void doTest() {
		for (int i = 0; i < THREAD_COUNT; i++) {
			executor.submit(new Worker());
		}

		long start = System.currentTimeMillis();
		startLatch.countDown();

		try {
			stopLatch.await();
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}

		cache.flushAll();

		long elapsed = System.currentTimeMillis() - start;

		System.out.println("");
		System.out.println("-----------------------------------");
		System.out.println("TEST COUNT=" + TEST_COUNT);
		System.out.println("RESULT EXPECTED=" + TEST_COUNT * THREAD_COUNT);
		System.out.println("RESULT=" + resultValue.get());
		System.out.println("ELAPSED=" + elapsed + "ms");
		System.out.println("-----------------------------------");

		Assert.assertEquals(TEST_COUNT * THREAD_COUNT, resultValue.get());
	}

	@Test
	public void runTest() {
		int testCount = 1;
		for (int i = 1; i <= testCount; i++) {
			initialize();
			doTest();
			System.out.println("PASSED (" + i + "/" + testCount + ")");
		}
	}
}
