package com.nhn.pinpoint.collector.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private final ExecutorService executor = Executors.newFixedThreadPool(3);

	private static final class Worker implements Runnable {
		private final CachedStatisticsDao dao;

		public Worker(CachedStatisticsDao dao) {
			this.dao = dao;
		}

		@Override
		public void run() {
			while (true) {
				dao.flushAll();
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void initialize() {
		executor.execute(new Worker(applicationMapStatisticsCalleeDao));
		executor.execute(new Worker(applicationMapStatisticsCallerDao));
		executor.execute(new Worker(applicationMapStatisticsDao));
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
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
		}));
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
