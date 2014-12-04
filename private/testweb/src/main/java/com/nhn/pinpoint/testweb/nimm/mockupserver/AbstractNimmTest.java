package com.nhn.pinpoint.testweb.nimm.mockupserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.nhncorp.lucy.nimm.connector.NimmConnector;
import com.nhncorp.lucy.nimm.connector.util.ThreadSleepUtils;
import com.nhncorp.lucy.nimm.mockupserver.MockUpServerSettler;

/**
 * <pre>
 * original source : com.nhncorp.lucy.nimm.connector.AbstractNimmTest
 * </pre>
 * 
 * @author netspider
 * 
 */
public abstract class AbstractNimmTest {
	private static final Logger log = Logger.getLogger(AbstractNimmTest.class.getName());

	protected MockUpServerSettler mockUpServerSettler;

	protected void initialize(final String configFileName) {
		boolean success = false;
		try {
			checkMockupServer();
			log.info("mockserver start");
			mockUpServerSettler = new MockUpServerSettler(configFileName);
			mockUpServerSettler.setUp();
			log.info("mockserver setup-end");
			success = true;
		} finally {
			if (success) {
				log.info("NimmConnector start");
				NimmConnector.registerMMNetDriver(configFileName);
			}
		}
	}

	protected void connectorFirstInitialize(final String configFileName) {
		checkMockupServer();
		mockUpServerSettler = new MockUpServerSettler(configFileName);

		Thread start = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000 * 5);
					log.info("mockserver start");
					mockUpServerSettler.setUp();
				} catch (Exception e) {
					log.log(Level.SEVERE, "mockserver start error", e);
				}
			}
		}, "mockserver start thread");
		start.setDaemon(true);
		start.start();

		log.info("NimmConnector start");
		NimmConnector.registerMMNetDriver(configFileName);

	}

	protected void checkMockupServer() {
		if (mockUpServerSettler != null) {
			throw new NullPointerException("mockupserver already started, testcase check");
		}
	}

	protected void reconnectInitialize(final String configFileName) {
		checkMockupServer();
		mockUpServerSettler = new MockUpServerSettler(configFileName);
		mockUpServerSettler.setUp();

		log.info("NimmConnector start");
		NimmConnector.registerMMNetDriver(configFileName);
		ThreadSleepUtils.sleepUninterruptibly(1000);

		Logger logger = Logger.getLogger(AbstractNimmTest.class.getName());
		logger.severe("mockupServer-down:start");
		mockUpServerSettler.setDown();
		logger.severe("mockupServer-down:end");
		ThreadSleepUtils.sleepUninterruptibly(2000);
		logger.severe("mockupServer-start");
		mockUpServerSettler = new MockUpServerSettler(configFileName);
		mockUpServerSettler.setUp();

		// Connector가 재접속할때까지 대기한다. reconnect 재시도 시간이 5초이므로, 5초이상 쉬어야 한다.
		ThreadSleepUtils.sleepUninterruptibly(1000 * 8);
	}

	protected void gracefulShutdownInitialize(final String configFileName) {
		checkMockupServer();
		mockUpServerSettler = new MockUpServerSettler(configFileName);
		mockUpServerSettler.setUp();

		log.info("NimmConnector start");
		NimmConnector.registerMMNetDriver(configFileName);
		ThreadSleepUtils.sleepUninterruptibly(2000);

		Logger.getLogger(AbstractNimmTest.class.getName()).severe("mockupServer-down:start");
		mockUpServerSettler.setGracefulDown();
		Logger.getLogger(AbstractNimmTest.class.getName()).severe("mockupServer-down:end");
		ThreadSleepUtils.sleepUninterruptibly(2000);
		Logger.getLogger(AbstractNimmTest.class.getName()).severe("mockupServer-start");
		mockUpServerSettler = new MockUpServerSettler(configFileName);
		mockUpServerSettler.setUp();

		// Connector가 재접속할때까지 대기한다. reconnect 재시도 시간이 5초이므로, 5초이상 쉬어야 한다.
		ThreadSleepUtils.sleepUninterruptibly(8000);

	}

	public void dispose() {
		try {
			log.info("NimmConnector dispose");
			NimmConnector.shutdownGracefully();
		} finally {
			if (mockUpServerSettler != null) {
				log.info("mockserver dispose");
				mockUpServerSettler.setDown();
				mockUpServerSettler = null;
			}
		}

	}

	public AbstractNimmTest() {
		super();
	}
}