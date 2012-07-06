package com.profiler.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

import com.profiler.logging.LogLevel;
import com.profiler.logging.Logger;

public class TomcatProfilerConfig {

	private static final Logger logger = Logger.getLogger(TomcatProfilerConfig.class);

	public static String SERVER_IP = "127.0.0.1";

	public static int AGENT_TCP_LISTEN_PORT = 9990;
	public static int SERVER_TCP_LISTEN_PORT = 9991;
	public static int REQUEST_TRANSACTION_DATA_LISTEN_PORT = 9995;
	public static int REQUEST_DATA_LISTEN_PORT = 9996;
	public static int JVM_DATA_LISTEN_PORT = 9997;

	public static long JVM_STAT_GAP = 5000L;
	public static long SERVER_CONNECT_RETRY_GAP = 1000L;

	public static LogLevel LOG_LEVEL = LogLevel.INFO;

	/**
	 * If sql query count is over 10000 it consumes Memory. So sqlHashSet uses
	 * CopyOnWriteArraySet. It is slow, but it is stable. Default set is false
	 * and it uses HashSet.
	 */
	public static boolean QUERY_COUNT_OVER_10000 = false;
	public static boolean JDBC_PROFILE = true;

	static {
		readConfigFile();
	}

	public static void readConfigFile() {
		String hippoConfigFileName = System.getProperty("hippo.config");

		if (hippoConfigFileName != null) {
			Properties prop = new Properties();
			try {
				FileReader reader = new FileReader(hippoConfigFileName);
				prop.load(reader);
				reader.close();
				setPropertyValues(prop);
			} catch (FileNotFoundException fnfe) {
				logger.error("%s file is not exists. Please check configuration.", hippoConfigFileName);
			} catch (Exception e) {
				logger.fatal(e.getMessage());
			}
		} else {
			logger.warn("hippo.config property is not set. Using default values");
		}
	}

	private static void setPropertyValues(Properties prop) {
		// TODO : use Properties defaultvalue instead of using temp variable.

		Object temp = null;

		if ((temp = prop.get("SERVER_IP")) != null) {
			SERVER_IP = temp.toString();
			logger.info("SERVER_IP=%s", SERVER_IP);
		}
		if ((temp = prop.get("AGENT_TCP_LISTEN_PORT")) != null) {
			AGENT_TCP_LISTEN_PORT = Integer.parseInt(temp.toString());
			logger.info("AGENT_TCP_LISTEN_PORT=%d", AGENT_TCP_LISTEN_PORT);
		}
		if ((temp = prop.get("SERVER_TCP_LISTEN_PORT")) != null) {
			SERVER_TCP_LISTEN_PORT = Integer.parseInt(temp.toString());
			logger.info("SERVER_TCP_LISTEN_PORT=%d", SERVER_TCP_LISTEN_PORT);
		}
		if ((temp = prop.get("REQUEST_TRANSACTION_DATA_LISTEN_PORT")) != null) {
			REQUEST_TRANSACTION_DATA_LISTEN_PORT = Integer.parseInt(temp.toString());
			logger.info("REQUEST_TRANSACTION_DATA_LISTEN_PORT=%d", REQUEST_TRANSACTION_DATA_LISTEN_PORT);
		}
		if ((temp = prop.get("REQUEST_DATA_LISTEN_PORT")) != null) {
			REQUEST_DATA_LISTEN_PORT = Integer.parseInt(temp.toString());
			logger.info("REQUEST_DATA_LISTEN_PORT=%d", REQUEST_DATA_LISTEN_PORT);
		}
		if ((temp = prop.get("JVM_DATA_LISTEN_PORT")) != null) {
			JVM_DATA_LISTEN_PORT = Integer.parseInt(temp.toString());
			logger.info("JVM_DATA_LISTEN_PORT=%d", JVM_DATA_LISTEN_PORT);
		}
		if ((temp = prop.get("JVM_STAT_GAP")) != null) {
			JVM_STAT_GAP = Long.parseLong(temp.toString());
			logger.info("JVM_STAT_GAP=%d", JVM_STAT_GAP);
		}
		if ((temp = prop.get("SERVER_CONNECT_RETRY_GAP")) != null) {
			SERVER_CONNECT_RETRY_GAP = Long.parseLong(temp.toString());
			logger.info("SERVER_CONNECT_RETRY_GAP=%d", SERVER_CONNECT_RETRY_GAP);
		}
		if ((temp = prop.get("QUERY_COUNT_OVER_10000")) != null) {
			QUERY_COUNT_OVER_10000 = Boolean.parseBoolean(temp.toString());
			logger.info("QUERY_COUNT_OVER_10000=%s", QUERY_COUNT_OVER_10000);
		}
		if ((temp = prop.get("JDBC_PROFILE")) != null) {
			JDBC_PROFILE = Boolean.parseBoolean(temp.toString());
			logger.info("JDBC_PROFILE=%s", JDBC_PROFILE);
		}
		if ((temp = prop.get("LOG_LEVEL")) != null) {
			LOG_LEVEL = LogLevel.valueOf(temp.toString());
			logger.info("LOG_LEVEL=%s", LOG_LEVEL);
		}

		logger.info("configuration loaded successfully.");
	}
}
