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
    private static final String DEFAULT_LISTEN_IP = "0.0.0.0";

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private String collectorTcpListenIp = DEFAULT_LISTEN_IP;
    private int collectorTcpListenPort = 9994;

    private String collectorUdpListenIp = DEFAULT_LISTEN_IP;
    private int collectorUdpListenPort = 9995;

    private int udpWorkerThread = 512;
    private int udpWorkerQueueSize = 1024 * 5;
    private int udpSocketReceiveBufferSize = 1024 * 4096;


    private String collectorUdpSpanListenIp = DEFAULT_LISTEN_IP;
    private int collectorUdpSpanListenPort = 9996;

    private int udpSpanWorkerThread = 512;
    private int udpSpanWorkerQueueSize = 1024 * 5;
    private int udpSpanSocketReceiveBufferSize = 1024 * 4096;


    public String getCollectorTcpListenIp() {
        return collectorTcpListenIp;
    }

    public int getCollectorTcpListenPort() {
        return collectorTcpListenPort;
    }



    public String getCollectorUdpListenIp() {
        return collectorUdpListenIp;
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

    public int getUdpSocketReceiveBufferSize() {
        return udpSocketReceiveBufferSize;
    }

    public String getCollectorUdpSpanListenIp() {
        return collectorUdpSpanListenIp;
    }

    public int getCollectorUdpSpanListenPort() {
		return collectorUdpSpanListenPort;
	}


	public int getUdpSpanWorkerThread() {
        return udpSpanWorkerThread;
    }

    public void setUdpSpanWorkerThread(int udpSpanWorkerThread) {
        this.udpSpanWorkerThread = udpSpanWorkerThread;
    }

    public int getUdpSpanWorkerQueueSize() {
        return udpSpanWorkerQueueSize;
    }

    public void setUdpSpanWorkerQueueSize(int udpSpanWorkerQueueSize) {
        this.udpSpanWorkerQueueSize = udpSpanWorkerQueueSize;
    }

    public int getUdpSpanSocketReceiveBufferSize() {
        return udpSpanSocketReceiveBufferSize;
    }

    public void setUdpSpanSocketReceiveBufferSize(int udpSpanSocketReceiveBufferSize) {
        this.udpSpanSocketReceiveBufferSize = udpSpanSocketReceiveBufferSize;
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
            readPropertyValues(prop);
        } catch (FileNotFoundException fe) {
            logger.error("File '{}' is not exists. Please check configuration.", configFileName, fe);
        } catch (Exception e) {
            logger.error("File '{}' error. Please check configuration.", configFileName, e);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(properties);

        readPropertyValues(this.properties);
    }

    private void readPropertyValues(Properties properties) {
        this.collectorTcpListenIp = readString(properties, "collectorTcpListenIp", DEFAULT_LISTEN_IP);
        this.collectorTcpListenPort = readInt(properties, "collectorTcpListenPort", collectorTcpListenPort);

        this.collectorUdpListenIp = readString(properties, "collectorUdpListenIp", DEFAULT_LISTEN_IP);
        this.collectorUdpListenPort = readInt(properties, "collectorUdpListenPort", collectorUdpListenPort);

        this.udpWorkerThread = readInt(properties, "udpWorkerThread", udpWorkerThread);
        this.udpWorkerQueueSize = readInt(properties, "udpWorkerQueueSize", udpWorkerQueueSize);
        this.udpSocketReceiveBufferSize = readInt(properties, "udpSocketReceiveBufferSize", udpSocketReceiveBufferSize);


        this.collectorUdpSpanListenIp = readString(properties, "collectorUdpSpanListenIp", DEFAULT_LISTEN_IP);
        this.collectorUdpSpanListenPort = readInt(properties, "collectorUdpSpanListenPort", collectorUdpSpanListenPort);

        this.udpSpanWorkerThread = readInt(properties, "udpSpanWorkerThread", udpSpanWorkerThread);
        this.udpSpanWorkerQueueSize = readInt(properties, "udpSpanWorkerQueueSize", udpSpanWorkerQueueSize);
        this.udpSpanSocketReceiveBufferSize = readInt(properties, "udpSpanSocketReceiveBufferSize", udpSpanSocketReceiveBufferSize);
        logger.info("Pinpoint configuration successfully loaded.");
    }

    private String readString(Properties properties, String propertyName, String defaultValue) {
        final String result = properties.getProperty(propertyName, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result ;
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
        sb.append("collectorTcpListenIp='").append(collectorTcpListenIp).append('\'');
        sb.append(", collectorTcpListenPort=").append(collectorTcpListenPort);
        sb.append(", collectorUdpListenIp='").append(collectorUdpListenIp).append('\'');
        sb.append(", collectorUdpListenPort=").append(collectorUdpListenPort);
        sb.append(", udpWorkerThread=").append(udpWorkerThread);
        sb.append(", udpWorkerQueueSize=").append(udpWorkerQueueSize);
        sb.append(", udpSocketReceiveBufferSize=").append(udpSocketReceiveBufferSize);
        sb.append(", collectorUdpSpanListenIp='").append(collectorUdpSpanListenIp).append('\'');
        sb.append(", collectorUdpSpanListenPort=").append(collectorUdpSpanListenPort);
        sb.append(", udpSpanWorkerThread=").append(udpSpanWorkerThread);
        sb.append(", udpSpanWorkerQueueSize=").append(udpSpanWorkerQueueSize);
        sb.append(", udpSpanSocketReceiveBufferSize=").append(udpSpanSocketReceiveBufferSize);
        sb.append('}');
        return sb.toString();
    }
}
