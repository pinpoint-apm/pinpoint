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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;

/**
 * @author emeroad
 */
public class CollectorConfiguration implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String CONFIG_FILE_NAME = "pinpoint-collector.properties";
    private static final String DEFAULT_LISTEN_IP = "0.0.0.0";

    private Properties properties;

    private SimpleProperty SYSTEM_PROPERTY = SystemProperty.INSTANCE;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private String tcpListenIp = DEFAULT_LISTEN_IP;
    private int tcpListenPort;
    
    private int tcpWorkerThread;
    private int tcpWorkerQueueSize;
    private boolean tcpWorkerMonitor;

    private String udpStatListenIp = DEFAULT_LISTEN_IP;
    private int udpStatListenPort;

    private int udpStatWorkerThread;
    private int udpStatWorkerQueueSize;
    private boolean udpStatWorkerMonitor;
    private int udpStatSocketReceiveBufferSize;

    private String udpSpanListenIp = DEFAULT_LISTEN_IP;
    private int udpSpanListenPort;

    private int udpSpanWorkerThread;
    private int udpSpanWorkerQueueSize;
    private boolean udpSpanWorkerMonitor;
    private int udpSpanSocketReceiveBufferSize;
    
    private int agentEventWorkerThreadSize;
    private int agentEventWorkerQueueSize;
    
    private List<String> l4IpList = Collections.emptyList();

    private boolean clusterEnable;
    private String clusterAddress;
    private int clusterSessionTimeout;

    private String clusterListenIp;
    private int clusterListenPort;

    public String getTcpListenIp() {
        return tcpListenIp;
    }

    public void setTcpListenIp(String tcpListenIp) {
        this.tcpListenIp = tcpListenIp;
    }

    public int getTcpListenPort() {
        return tcpListenPort;
    }

    public void setTcpListenPort(int tcpListenPort) {
        this.tcpListenPort = tcpListenPort;
    }

    public int getTcpWorkerThread() {
        return tcpWorkerThread;
    }
    
    public void setTcpWorkerThread(int tcpWorkerThread) {
        this.tcpWorkerThread = tcpWorkerThread;
    }
    
    public int getTcpWorkerQueueSize() {
        return tcpWorkerQueueSize;
    }
    
    public void setTcpWorkerQueueSize(int tcpWorkerQueueSize) {
        this.tcpWorkerQueueSize = tcpWorkerQueueSize;
    }

    public boolean isTcpWorkerMonitor() {
        return tcpWorkerMonitor;
    }

    public void setTcpWorkerMonitor(boolean tcpWorkerMonitor) {
        this.tcpWorkerMonitor = tcpWorkerMonitor;
    }

    public String getUdpStatListenIp() {
        return udpStatListenIp;
    }

    public void setUdpStatListenIp(String udpStatListenIp) {
        this.udpStatListenIp = udpStatListenIp;
    }

    public int getUdpStatListenPort() {
        return udpStatListenPort;
    }

    public void setUdpStatListenPort(int udpStatListenPort) {
        this.udpStatListenPort = udpStatListenPort;
    }

    public int getUdpStatWorkerThread() {
        return udpStatWorkerThread;
    }

    public void setUdpStatWorkerThread(int udpStatWorkerThread) {
        this.udpStatWorkerThread = udpStatWorkerThread;
    }

    public int getUdpStatWorkerQueueSize() {
        return udpStatWorkerQueueSize;
    }

    public void setUdpStatWorkerQueueSize(int udpStatWorkerQueueSize) {
        this.udpStatWorkerQueueSize = udpStatWorkerQueueSize;
    }

    public boolean isUdpStatWorkerMonitor() {
        return udpStatWorkerMonitor;
    }

    public void setUdpStatWorkerMonitor(boolean udpStatWorkerMonitor) {
        this.udpStatWorkerMonitor = udpStatWorkerMonitor;
    }

    public int getUdpStatSocketReceiveBufferSize() {
        return udpStatSocketReceiveBufferSize;
    }

    public void setUdpStatSocketReceiveBufferSize(int udpStatSocketReceiveBufferSize) {
        this.udpStatSocketReceiveBufferSize = udpStatSocketReceiveBufferSize;
    }

    public String getUdpSpanListenIp() {
        return udpSpanListenIp;
    }

    public void setUdpSpanListenIp(String udpSpanListenIp) {
        this.udpSpanListenIp = udpSpanListenIp;
    }

    public int getUdpSpanListenPort() {
        return udpSpanListenPort;
    }

    public void setUdpSpanListenPort(int udpSpanListenPort) {
        this.udpSpanListenPort = udpSpanListenPort;
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

    public boolean isUdpSpanWorkerMonitor() {
        return udpSpanWorkerMonitor;
    }

    public void setUdpSpanWorkerMonitor(boolean udpSpanWorkerMonitor) {
        this.udpSpanWorkerMonitor = udpSpanWorkerMonitor;
    }

    public int getUdpSpanSocketReceiveBufferSize() {
        return udpSpanSocketReceiveBufferSize;
    }

    public void setUdpSpanSocketReceiveBufferSize(int udpSpanSocketReceiveBufferSize) {
        this.udpSpanSocketReceiveBufferSize = udpSpanSocketReceiveBufferSize;
    }

    public int getAgentEventWorkerThreadSize() {
        return this.agentEventWorkerThreadSize;
    }

    public void setAgentEventWorkerThreadSize(int agentEventWorkerThreadSize) {
        this.agentEventWorkerThreadSize = agentEventWorkerThreadSize;
    }
    
    public int getAgentEventWorkerQueueSize() {
        return agentEventWorkerQueueSize;
    }

    public void setAgentEventWorkerQueueSize(int agentEventWorkerQueueSize) {
        this.agentEventWorkerQueueSize = agentEventWorkerQueueSize;
    }

    public List<String> getL4IpList() {
        return l4IpList;
    }

    public void setL4IpList(List<String> l4IpList) {
        this.l4IpList = l4IpList;
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

    public String getClusterListenIp() {
        return clusterListenIp;
    }

    public void setClusterListenIp(String clusterListenIp) {
        this.clusterListenIp = clusterListenIp;
    }

    public int getClusterListenPort() {
        return clusterListenPort;
    }

    public void setClusterListenPort(int clusterListenPort) {
        this.clusterListenPort = clusterListenPort;
    }

    public void readConfigFile() {

        // may be useful for some kind of standalone like testcase. It should be modified to read a classpath for testcase.
        String configFileName = SYSTEM_PROPERTY.getProperty(CONFIG_FILE_NAME);
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
        
        this.tcpWorkerThread = readInt(properties, "collector.tcpWorkerThread", 128);
        this.tcpWorkerQueueSize = readInt(properties, "collector.tcpWorkerQueueSize", 1024 * 5);
        this.tcpWorkerMonitor = readBoolean(properties, "collector.tcpWorker.monitor");

        this.udpStatListenIp = readString(properties, "collector.udpStatListenIp", DEFAULT_LISTEN_IP);
        this.udpStatListenPort = readInt(properties, "collector.udpStatListenPort", 9995);

        this.udpStatWorkerThread = readInt(properties, "collector.udpStatWorkerThread", 128);
        this.udpStatWorkerQueueSize = readInt(properties, "collector.udpStatWorkerQueueSize", 1024);
        this.udpStatWorkerMonitor = readBoolean(properties, "collector.udpStatWorker.monitor");
        this.udpStatSocketReceiveBufferSize = readInt(properties, "collector.udpStatSocketReceiveBufferSize", 1024 * 4096);

        this.udpSpanListenIp = readString(properties, "collector.udpSpanListenIp", DEFAULT_LISTEN_IP);
        this.udpSpanListenPort = readInt(properties, "collector.udpSpanListenPort", udpSpanListenPort);

        this.udpSpanWorkerThread = readInt(properties, "collector.udpSpanWorkerThread", 256);
        this.udpSpanWorkerQueueSize = readInt(properties, "collector.udpSpanWorkerQueueSize", 1024 * 5);
        this.udpSpanWorkerMonitor = readBoolean(properties, "collector.udpSpanWorker.monitor");
        this.udpSpanSocketReceiveBufferSize = readInt(properties, "collector.udpSpanSocketReceiveBufferSize", 1024 * 4096);
        
        this.agentEventWorkerThreadSize = readInt(properties, "collector.agentEventWorker.threadSize", 32);
        this.agentEventWorkerQueueSize = readInt(properties, "collector.agentEventWorker.queueSize", 1024 * 5);
        
        String[] l4Ips = StringUtils.split(readString(properties, "collector.l4.ip", null), ",");
        if (l4Ips == null) {
            this.l4IpList = Collections.emptyList();
        } else {
            this.l4IpList = new ArrayList<>(l4Ips.length);
            for (String l4Ip : l4Ips) {
                if (!StringUtils.isEmpty(l4Ip)) {
                    this.l4IpList.add(StringUtils.trim(l4Ip));
                }
            }
        }
        
        this.clusterEnable = readBoolean(properties, "cluster.enable");
        this.clusterAddress = readString(properties, "cluster.zookeeper.address", "");
        this.clusterSessionTimeout = readInt(properties, "cluster.zookeeper.sessiontimeout", -1);

        this.clusterListenIp = readString(properties, "cluster.listen.ip", "");
        this.clusterListenPort = readInt(properties, "cluster.listen.port", -1);
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
        final int result = NumberUtils.toInt(value, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result;
    }

    private long readLong(Properties properties, String propertyName, long defaultValue) {
        final String value = properties.getProperty(propertyName);
        final long result = NumberUtils.toLong(value, defaultValue);
        if (logger.isInfoEnabled()) {
            logger.info("{}={}", propertyName, result);
        }
        return result;
    }

    private boolean readBoolean(Properties properties, String propertyName) {
        final String value = properties.getProperty(propertyName);
        
        // if a default value will be needed afterwards, may match string value instead of Utils.
        // for now stay unmodified because of no need.

        final boolean result = Boolean.valueOf(value);
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
        sb.append(", tcpWorkerThread=").append(tcpWorkerThread);
        sb.append(", tcpWorkerQueueSize=").append(tcpWorkerQueueSize);
        sb.append(", tcpWorkerMonitor=").append(tcpWorkerMonitor);
        sb.append(", udpStatListenIp='").append(udpStatListenIp).append('\'');
        sb.append(", udpStatListenPort=").append(udpStatListenPort);
        sb.append(", udpStatWorkerThread=").append(udpStatWorkerThread);
        sb.append(", udpStatWorkerQueueSize=").append(udpStatWorkerQueueSize);
        sb.append(", udpStatWorkerMonitor=").append(udpStatWorkerMonitor);
        sb.append(", udpStatSocketReceiveBufferSize=").append(udpStatSocketReceiveBufferSize);
        sb.append(", udpSpanListenIp='").append(udpSpanListenIp).append('\'');
        sb.append(", udpSpanListenPort=").append(udpSpanListenPort);
        sb.append(", udpSpanWorkerThread=").append(udpSpanWorkerThread);
        sb.append(", udpSpanWorkerQueueSize=").append(udpSpanWorkerQueueSize);
        sb.append(", udpSpanWorkerMonitor=").append(udpSpanWorkerMonitor);
        sb.append(", udpSpanSocketReceiveBufferSize=").append(udpSpanSocketReceiveBufferSize);
        sb.append(", agentEventWorkerThreadSize=").append(agentEventWorkerThreadSize);
        sb.append(", agentEventWorkerQueueSize=").append(agentEventWorkerQueueSize);
        sb.append(", l4IpList=").append(l4IpList);
        sb.append(", clusterEnable=").append(clusterEnable);
        sb.append(", clusterAddress=").append(clusterAddress);
        sb.append(", clusterSessionTimeout=").append(clusterSessionTimeout);
        sb.append(", clusterListenIp=").append(clusterListenIp);
        sb.append(", clusterListenPort=").append(clusterListenPort);

        sb.append('}');
        return sb.toString();
    }

}
