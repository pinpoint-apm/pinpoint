package com.nhn.pinpoint.collector.dao;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class AutoFlusher {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ScheduledExecutorService executor;

    private long flushPeriod = 1000;

	@Autowired
	private List<CachedStatisticsDao> cachedStatisticsDaoList;

    public long getFlushPeriod() {
        return flushPeriod;
    }

    public void setFlushPeriod(long flushPeriod) {
        this.flushPeriod = flushPeriod;
    }

    private static final class Worker implements Runnable {
    	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		private final CachedStatisticsDao dao;

		public Worker(CachedStatisticsDao dao) {
			this.dao = dao;
		}

		@Override
		public void run() {
			try {
				dao.flushAll();
			} catch (Throwable th) {
				logger.error("AutoFlusherWorker failed.", th);
			}
		}
	}

	public void initialize() {
		if (cachedStatisticsDaoList == null || cachedStatisticsDaoList.isEmpty()) {
			return;
		}

		ThreadFactory threadFactory = PinpointThreadFactory.createThreadFactory(this.getClass().getSimpleName());
		executor = Executors.newScheduledThreadPool(cachedStatisticsDaoList.size(), threadFactory);
		for (CachedStatisticsDao dao : cachedStatisticsDaoList) {
			executor.scheduleAtFixedRate(new Worker(dao), 0L, flushPeriod, TimeUnit.MILLISECONDS);
		}
		logger.info("Auto flusher initialized.");
	}

	public void shutdown() {
		logger.info("Shutdown auto flusher.");
		shutdownExecutor();
		for (CachedStatisticsDao dao : cachedStatisticsDaoList) {
			dao.flushAll();
		}
	}

	private void shutdownExecutor() {
		executor.shutdown();
		try {
			executor.awaitTermination(3000 + flushPeriod, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void setCachedStatisticsDaoList(List<CachedStatisticsDao> cachedStatisticsDaoList) {
		this.cachedStatisticsDaoList = cachedStatisticsDaoList;
	}
}
