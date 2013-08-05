package com.nhn.pinpoint.collector.config;

import com.nhn.pinpoint.common.util.PropertyUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Properties;

public class CollectorConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CONFIG_FILE_PROPERTY_NAME = "pinpointserver.config";

    private int collectorTcpListenPort = 9994;
    private int collectorUdpListenPort = 9995;

    private int udpWorkerThread = 512;
    private int udpWorkerQueueSize = 1024 * 5;

    public int getCollectorTcpListenPort() {
        return collectorTcpListenPort;
    }

    public int getCollectorUdpListenPort() {
        return collectorUdpListenPort;
    }

    public int getUdpWorkerThread() {
        return udpWorkerThread;
    }

    public int getUdpWorkerQueueSize() {
        return udpWorkerQueueSize;
    }

    public void readConfigFile() {
        String configFileName = System.getProperty(CONFIG_FILE_PROPERTY_NAME);
        if (configFileName == null) {
            logger.warn("Property is not set. Using default values. PROPERTY_NAME={}, defaultValue={}", CONFIG_FILE_PROPERTY_NAME, this);
            return;
        }

        try {
            Properties prop = PropertyUtils.readProperties(configFileName);
            setPropertyValues(prop);
        } catch (FileNotFoundException fe) {
            logger.error("File '{}' is not exists. Please check configuration.", configFileName, fe);
        } catch (Exception e) {
            logger.error("File '{}' error. Please check configuration.", configFileName, e);
        }

    }

    private void setPropertyValues(Properties properties) {
        this.collectorTcpListenPort = readInt(properties, "collectorTcpListenPort", collectorTcpListenPort);
        this.collectorUdpListenPort = readInt(properties, "collectorUdpListenPort", collectorUdpListenPort);

        this.udpWorkerThread = readInt(properties, "udpWorkerThread", udpWorkerThread);
        this.udpWorkerQueueSize = readInt(properties, "udpWorkerQueueSize", udpWorkerQueueSize);

        logger.info("Pinpoint configuration successfully loaded.");
    }

    private int readInt(Properties properties, String propertyName, int defaultValue) {
        final String value = properties.getProperty(propertyName);
        int result = NumberUtils.toInt(value, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CollectorConfiguration{");
        sb.append("collectorTcpListenPort=").append(collectorTcpListenPort);
        sb.append(", collectorUdpListenPort=").append(collectorUdpListenPort);
        sb.append(", udpWorkerThread=").append(udpWorkerThread);
        sb.append(", udpWorkerQueueSize=").append(udpWorkerQueueSize);
        sb.append('}');
        return sb.toString();
    }
}
