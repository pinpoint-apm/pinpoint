package com.nhn.pinpoint.collector.config;

import com.nhn.pinpoint.common.util.PropertyUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.FileNotFoundException;
import java.util.Properties;

public class CollectorConfiguration implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CONFIG_FILE_NAME = "pinpoint-collector.properties";

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private int collectorTcpListenPort = 9994;
    private int collectorUdpListenPort = 9995;
    private int collectorStatListenPort = 9996;

    private int udpWorkerThread = 512;
    private int udpWorkerQueueSize = 1024 * 5;

    private int udpReceiveBufferSize = 1024 * 4096;
    
    public int getCollectorTcpListenPort() {
        return collectorTcpListenPort;
    }

    public void setCollectorTcpListenPort(int collectorTcpListenPort) {
        this.collectorTcpListenPort = collectorTcpListenPort;
    }

    public int getCollectorUdpListenPort() {
        return collectorUdpListenPort;
    }

    public void setCollectorUdpListenPort(int collectorUdpListenPort) {
        this.collectorUdpListenPort = collectorUdpListenPort;
    }
    
    public int getCollectorStatListenPort() {
		return collectorStatListenPort;
	}

	public void setCollectorStatListenPort(int collectorStatListenPort) {
		this.collectorStatListenPort = collectorStatListenPort;
	}

	public int getUdpWorkerThread() {
        return udpWorkerThread;
    }

    public void setUdpWorkerThread(int udpWorkerThread) {
        this.udpWorkerThread = udpWorkerThread;
    }

    public int getUdpWorkerQueueSize() {
        return udpWorkerQueueSize;
    }

    public void setUdpWorkerQueueSize(int udpWorkerQueueSize) {
        this.udpWorkerQueueSize = udpWorkerQueueSize;
    }

    public int getUdpReceiveBufferSize() {
        return udpReceiveBufferSize;
    }

    public void setUdpReceiveBufferSize(int udpReceiveBufferSize) {
        this.udpReceiveBufferSize = udpReceiveBufferSize;
    }


    public void readConfigFile() {
        //    testcase와 같이 단독으로 사용할 경우 해당 api를 사용하면 좋을듯. testcase에서 쓸려면 classpath를 읽도록 고쳐야 될거임.
        String configFileName = System.getProperty(CONFIG_FILE_NAME);
        if (configFileName == null) {
            logger.warn("Property is not set. Using default values. PROPERTY_NAME={}, defaultValue={}", CONFIG_FILE_NAME, this);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(properties);

        setPropertyValues(this.properties);
    }

    private void setPropertyValues(Properties properties) {
        this.collectorTcpListenPort = readInt(properties, "collectorTcpListenPort", collectorTcpListenPort);
        this.collectorUdpListenPort = readInt(properties, "collectorUdpListenPort", collectorUdpListenPort);
        this.collectorStatListenPort = readInt(properties, "collectorStatListenPort", collectorStatListenPort);

        this.udpWorkerThread = readInt(properties, "udpWorkerThread", udpWorkerThread);
        this.udpWorkerQueueSize = readInt(properties, "udpWorkerQueueSize", udpWorkerQueueSize);
        this.udpReceiveBufferSize = readInt(properties, "udpSocketReceiverBufferSize", udpReceiveBufferSize);
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
        sb.append(", collectorStatListenPort=").append(collectorStatListenPort);
        sb.append(", udpWorkerThread=").append(udpWorkerThread);
        sb.append(", udpWorkerQueueSize=").append(udpWorkerQueueSize);
        sb.append('}');
        return sb.toString();
    }


}
