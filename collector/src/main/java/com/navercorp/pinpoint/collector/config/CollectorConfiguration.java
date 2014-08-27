package com.nhn.pinpoint.collector.config;

import com.nhn.pinpoint.common.util.PropertyUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * @author emeroad
 */
public class CollectorConfiguration implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CONFIG_FILE_NAME = "pinpoint-collector.properties";
    private static final String DEFAULT_LISTEN_IP = "0.0.0.0";

    private Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private String tcpListenIp = DEFAULT_LISTEN_IP;
    private int tcpListenPort;

    private String udpStatListenIp = DEFAULT_LISTEN_IP;
    private int udpStatListenPort;

    private int udpStatWorkerThread;
    private int udpStatWorkerQueueSize;
    private int udpStatSocketReceiveBufferSize;


    private String udpSpanListenIp = DEFAULT_LISTEN_IP;
    private int udpSpanListenPort;

    private int udpSpanWorkerThread;
    private int udpSpanWorkerQueueSize;
    private int udpSpanSocketReceiveBufferSize;


    public String getTcpListenIp() {
        return tcpListenIp;
    }

    public int getTcpListenPort() {
        return tcpListenPort;
    }



    public String getUdpStatListenIp() {
        return udpStatListenIp;
    }

    public int getUdpStatListenPort() {
        return udpStatListenPort;
    }

    public int getUdpStatWorkerThread() {
        return udpStatWorkerThread;
    }

    public int getUdpStatWorkerQueueSize() {
        return udpStatWorkerQueueSize;
    }

    public int getUdpStatSocketReceiveBufferSize() {
        return udpStatSocketReceiveBufferSize;
    }

    public String getUdpSpanListenIp() {
        return udpSpanListenIp;
    }

    public int getUdpSpanListenPort() {
		return udpSpanListenPort;
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
        logger.info("pinpoint-collector.properties read.");
        this.tcpListenIp = readString(properties, "collector.tcpListenIp", DEFAULT_LISTEN_IP);
        this.tcpListenPort = readInt(properties, "collector.tcpListenPort", 9994);


        this.udpStatListenIp = readString(properties, "collector.udpStatListenIp", DEFAULT_LISTEN_IP);
        this.udpStatListenPort = readInt(properties, "collector.udpStatListenPort", 9995);

        this.udpStatWorkerThread = readInt(properties, "collector.udpStatWorkerThread", 128);
        this.udpStatWorkerQueueSize = readInt(properties, "collector.udpStatWorkerQueueSize", 1024);
        this.udpStatSocketReceiveBufferSize = readInt(properties, "collector.udpStatSocketReceiveBufferSize", 1024 * 4096);


        this.udpSpanListenIp = readString(properties, "collector.udpSpanListenIp", DEFAULT_LISTEN_IP);
        this.udpSpanListenPort = readInt(properties, "collector.udpSpanListenPort", udpSpanListenPort);

        this.udpSpanWorkerThread = readInt(properties, "collector.udpSpanWorkerThread", 256);
        this.udpSpanWorkerQueueSize = readInt(properties, "collector.udpSpanWorkerQueueSize", 1024 * 5);
        this.udpSpanSocketReceiveBufferSize = readInt(properties, "collector.udpSpanSocketReceiveBufferSize", 1024 * 4096);
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
        sb.append("tcpListenIp='").append(tcpListenIp).append('\'');
        sb.append(", tcpListenPort=").append(tcpListenPort);
        sb.append(", udpStatListenIp='").append(udpStatListenIp).append('\'');
        sb.append(", udpStatListenPort=").append(udpStatListenPort);
        sb.append(", udpStatWorkerThread=").append(udpStatWorkerThread);
        sb.append(", udpStatWorkerQueueSize=").append(udpStatWorkerQueueSize);
        sb.append(", udpStatSocketReceiveBufferSize=").append(udpStatSocketReceiveBufferSize);
        sb.append(", udpSpanListenIp='").append(udpSpanListenIp).append('\'');
        sb.append(", udpSpanListenPort=").append(udpSpanListenPort);
        sb.append(", udpSpanWorkerThread=").append(udpSpanWorkerThread);
        sb.append(", udpSpanWorkerQueueSize=").append(udpSpanWorkerQueueSize);
        sb.append(", udpSpanSocketReceiveBufferSize=").append(udpSpanSocketReceiveBufferSize);
        sb.append('}');
        return sb.toString();
    }
}
