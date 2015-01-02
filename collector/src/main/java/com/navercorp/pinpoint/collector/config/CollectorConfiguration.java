/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;

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

//    cluster.zookeeper.address=dev.zk.pinpoint.navercorp.com
//            cluster.zookeeper.sessiontimeout=3000
    
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

    private boolean clusterEnable;
    private String clusterAddress;
    private int clusterSessionTimeout;

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

    public boolean isClusterEnable() {
        return clusterEnable;
    }

    public void setClusterEnable(boolean clusterEnable) {
        this.clusterEnable = clusterEnable;
    }

    public String getClusterAddress() {
        return clusterAddress;
    }

    public void setClusterAddress(String clusterAddress) {
        this.clusterAddress = clusterAddress;
    }

    public int getClusterSessionTimeout() {
        return clusterSessionTimeout;
    }

    public void setClusterSessionTimeout(int clusterSessionTimeout) {
        this.clusterSessionTimeout = clusterSessionTimeout;
    }

    public void readConfigFile() {

        // may be useful for some kind of standalone like testcase. It should be modified to read a classpath for testcase.
        String configFileName = System.getProperty(CONFIG_FILE_NAME);
        if (configFileName == null) {
            logger.warn("Property is not set. Using default values. PROPERTY_NAME={}, defaultValue={}", CONFIG_FILE_NAME, this);
            return;
        }

        try {
            Properties prop = PropertyUtils.loadProperty(configFileName);
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
        
        this.clusterEnable = readBoolen(properties, "cluster.enable");
        this.clusterAddress = readString(properties, "cluster.zookeeper.address", "");
        this.clusterSessionTimeout = readInt(properties, "cluster.zookeeper.sessiontimeout", -1);
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
    
    private boolean readBoolen(Properties properties, String propertyName) {
        final String value = properties.getProperty(propertyName);
        
        // if a default value will be needed afterwards, may match string value instead of Utils.
        // for now stay unmodified because of no need.

        boolean result = Boolean.valueOf(value);
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
        sb.append(", clusterEnable=").append(clusterEnable);
        sb.append(", clusterAddress=").append(clusterAddress);
        sb.append(", clusterSessionTimeout=").append(clusterSessionTimeout);
        
        sb.append('}');
        return sb.toString();
    }

}
