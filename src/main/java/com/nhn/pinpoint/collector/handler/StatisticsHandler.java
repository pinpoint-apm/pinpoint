package com.nhn.pinpoint.collector.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.collector.dao.ApplicationStatisticsDao;
import com.nhn.pinpoint.collector.dao.CachedStatisticsDao;

/**
 * 
 * @author netspider
 * 
 */
public class StatisticsHandler {

	private static final Logger logger = LoggerFactory.getLogger("StatisticsHandler");

	@Autowired
	private ApplicationStatisticsDao applicationMapStatisticsDao;

	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;

    private final ThreadFactory threadFactory = PinpointThreadFactory.createThreadFactory(this.getClass().getSimpleName());
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3, threadFactory);

	private static final class Worker implements Runnable {
		private final CachedStatisticsDao dao;

		public Worker(CachedStatisticsDao dao) {
			this.dao = dao;
		}

		@Override
		public void run() {
			dao.flushAll();
		}
	}

	public void initialize() {
		executor.scheduleAtFixedRate(new Worker(applicationMapStatisticsCalleeDao), 0L, 1000L, TimeUnit.MILLISECONDS);
		executor.scheduleAtFixedRate(new Worker(applicationMapStatisticsCallerDao), 0L, 1000L, TimeUnit.MILLISECONDS);
		executor.scheduleAtFixedRate(new Worker(applicationMapStatisticsDao), 0L, 1000L, TimeUnit.MILLISECONDS);
	}

	public void shutdown() {

        shutdownExecutor();

        logger.debug("Flush all statistics.");
		if (applicationMapStatisticsDao != null) {
			applicationMapStatisticsDao.flushAll();
		}
		if (applicationMapStatisticsCallerDao != null) {
			applicationMapStatisticsCallerDao.flushAll();
		}
		if (applicationMapStatisticsCalleeDao != null) {
			applicationMapStatisticsCalleeDao.flushAll();
		}

	}

    private void shutdownExecutor() {
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void updateCallee(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		applicationMapStatisticsCalleeDao.update(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType, calleeHost, elapsed, isError);
	}

	public void updateCaller(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError) {
		applicationMapStatisticsCallerDao.update(calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost, elapsed, isError);
	}

	public void updateApplication(String applicationName, short serviceType, String agentId, int elapsed, boolean isError) {
		applicationMapStatisticsDao.update(applicationName, serviceType, agentId, elapsed, isError);
	}
}
