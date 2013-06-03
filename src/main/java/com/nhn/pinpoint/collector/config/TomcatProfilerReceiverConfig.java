package com.nhn.pinpoint.collector.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatProfilerReceiverConfig {

	private static final Logger logger = LoggerFactory.getLogger("TomcatProfilerReceiverConfig");

	public static int SERVER_TCP_LISTEN_PORT = 9991;
	public static int SERVER_UDP_LISTEN_PORT = 9995;

	static {
		readConfigFile();
		printConfig();
	}

	public static void printConfig() {
		logger.info("SERVER_TCP_LISTEN_PORT={}", SERVER_TCP_LISTEN_PORT);
		logger.info("SERVER_UDP_LISTEN_PORT={}", SERVER_UDP_LISTEN_PORT);
	}

	public static void readConfigFile() {
		String configFileName = "pinpointserver.config";
		String pinpointConfigFileName = System.getProperty(configFileName);
		if (pinpointConfigFileName != null) {
			Properties prop = new Properties();
			try {
				FileReader reader = new FileReader(pinpointConfigFileName);
				prop.load(reader);
				reader.close();
				setPropertyValues(prop);
			} catch (FileNotFoundException fnfe) {
				logger.error("File '{}' is not exists. Please check configuration.", pinpointConfigFileName);
				fnfe.printStackTrace();
			} catch (Exception e) {
				logger.error("File '{}' is not exists. Please check configuration.", pinpointConfigFileName);
				e.printStackTrace();
			}
		} else {
			logger.warn("Property is not set. Using default values. filename={}", configFileName);
		}
	}

	private static void setPropertyValues(Properties prop) {
		Object temp = null;

		if ((temp = prop.get("SERVER_TCP_LISTEN_PORT")) != null)
			SERVER_TCP_LISTEN_PORT = Integer.parseInt(temp.toString());

		if ((temp = prop.get("SERVER_UDP_LISTEN_PORT")) != null)
			SERVER_UDP_LISTEN_PORT = Integer.parseInt(temp.toString());

		logger.info("Pinpoint configuration successfully loaded.");
	}
}
