package com.profiler.server.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatProfilerReceiverConfig {

	private static final Logger logger = LoggerFactory.getLogger("com.profiler.server.config.TomcatProfilerReceiverConfig");

	public static int SERVER_TCP_LISTEN_PORT = 9991;
	public static int SERVER_UDP_LISTEN_PORT = 9995;

	static {
		readConfigFile();
		printConfig();
	}

	public static void printConfig() {
		logger.info("SERVER_TCP_LISTEN_PORT=" + SERVER_TCP_LISTEN_PORT);
		logger.info("SERVER_UDP_LISTEN_PORT=" + SERVER_UDP_LISTEN_PORT);
	}

	public static void readConfigFile() {
		String configFileName = "hipposerver.config";
		String hippoConfigFileName = System.getProperty(configFileName);
		if (hippoConfigFileName != null) {
			Properties prop = new Properties();
			try {
				FileReader reader = new FileReader(hippoConfigFileName);
				prop.load(reader);
				reader.close();
				setPropertyValues(prop);
			} catch (FileNotFoundException fnfe) {
				logger.error("##### " + hippoConfigFileName + " file is not exists. Please check configuration.");
				fnfe.printStackTrace();
			} catch (Exception e) {
				logger.error("##### " + hippoConfigFileName + " file is not exists. Please check configuration.");
				e.printStackTrace();
			}
		} else {
			logger.info("##### " + configFileName + " property is not set. Using default values #####");
		}
	}

	private static void setPropertyValues(Properties prop) {
		Object temp = null;

		if ((temp = prop.get("SERVER_TCP_LISTEN_PORT")) != null)
			SERVER_TCP_LISTEN_PORT = Integer.parseInt(temp.toString());

		if ((temp = prop.get("SERVER_UDP_LISTEN_PORT")) != null)
			SERVER_UDP_LISTEN_PORT = Integer.parseInt(temp.toString());

		logger.info("##### Hippo Config Loaded successfully. #####");
	}
}
