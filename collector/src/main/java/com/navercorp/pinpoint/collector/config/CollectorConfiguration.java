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
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author emeroad
 */
public class CollectorConfiguration implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorConfiguration.class);

    private static final String CONFIG_FILE_NAME = "pinpoint-collector.properties";
    static final String DEFAULT_LISTEN_IP = "0.0.0.0";

    private Properties properties;

    private SimpleProperty SYSTEM_PROPERTY = SystemProperty.INSTANCE;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private int agentEventWorkerThreadSize;
    private int agentEventWorkerQueueSize;
    
    private List<String> l4IpList = Collections.emptyList();

    private boolean clusterEnable;
    private String clusterAddress;
    private int clusterSessionTimeout;

    private String clusterListenIp;
    private int clusterListenPort;

    private boolean flinkClusterEnable;
    private String flinkClusterZookeeperAddress;
    private int flinkClusterSessionTimeout;

    public void setFlinkClusterEnable(boolean flinkClusterEnable) {
        this.flinkClusterEnable = flinkClusterEnable;
    }

    public void setFlinkClusterZookeeperAddress(String flinkClusterZookeeperAddress) {
        this.flinkClusterZookeeperAddress = flinkClusterZookeeperAddress;
    }

    public void setFlinkClusterSessionTimeout(int flinkClusterSessionTimeout) {
        this.flinkClusterSessionTimeout = flinkClusterSessionTimeout;
    }

    public boolean isFlinkClusterEnable() {
        return flinkClusterEnable;
    }

    public String getFlinkClusterZookeeperAddress() {
        return flinkClusterZookeeperAddress;
    }

    public int getFlinkClusterSessionTimeout() {
        return flinkClusterSessionTimeout;
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
            LOGGER.warn("Property is not set. Using default values. PROPERTY_NAME={}, defaultValue={}", CONFIG_FILE_NAME, this);
            return;
        }

        try {
            Properties prop = PropertyUtils.loadProperty(configFileName);
            readPropertyValues(prop);
        } catch (FileNotFoundException fe) {
            LOGGER.error("File '{}' is not exists. Please check configuration.", configFileName, fe);
        } catch (Exception e) {
            LOGGER.error("File '{}' error. Please check configuration.", configFileName, e);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(properties);
        readPropertyValues(this.properties);
    }

    protected  void readPropertyValues(Properties properties) {
        LOGGER.info("pinpoint-collector.properties read.");

        this.agentEventWorkerThreadSize = readInt(properties, "collector.agentEventWorker.threadSize", 32);
        this.agentEventWorkerQueueSize = readInt(properties, "collector.agentEventWorker.queueSize", 1024 * 5);

        this.flinkClusterEnable = readBoolean(properties, "flink.cluster.enable");
        this.flinkClusterZookeeperAddress = readString(properties, "flink.cluster.zookeeper.address", "");
        this.flinkClusterSessionTimeout = readInt(properties, "flink.cluster.zookeeper.sessiontimeout", -1);
        
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

    protected static String readString(Properties properties, String propertyName, String defaultValue) {
        final String result = properties.getProperty(propertyName, defaultValue);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result ;
    }

    protected static int readInt(Properties properties, String propertyName, int defaultValue) {
        final String value = properties.getProperty(propertyName);
        final int result = NumberUtils.toInt(value, defaultValue);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result;
    }

    protected static long readLong(Properties properties, String propertyName, long defaultValue) {
        final String value = properties.getProperty(propertyName);
        final long result = NumberUtils.toLong(value, defaultValue);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result;
    }

    protected static boolean readBoolean(Properties properties, String propertyName) {
        final String value = properties.getProperty(propertyName);
        
        // if a default value will be needed afterwards, may match string value instead of Utils.
        // for now stay unmodified because of no need.

        final boolean result = Boolean.valueOf(value);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}={}", propertyName, result);
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CollectorConfiguration{");
        sb.append("agentEventWorkerThreadSize=").append(agentEventWorkerThreadSize);
        sb.append(", agentEventWorkerQueueSize=").append(agentEventWorkerQueueSize);
        sb.append(", l4IpList=").append(l4IpList);
        sb.append(", clusterEnable=").append(clusterEnable);
        sb.append(", clusterAddress='").append(clusterAddress).append('\'');
        sb.append(", clusterSessionTimeout=").append(clusterSessionTimeout);
        sb.append(", clusterListenIp='").append(clusterListenIp).append('\'');
        sb.append(", clusterListenPort=").append(clusterListenPort);
        sb.append(", flinkClusterEnable=").append(flinkClusterEnable);
        sb.append(", flinkClusterZookeeperAddress='").append(flinkClusterZookeeperAddress).append('\'');
        sb.append(", flinkClusterSessionTimeout=").append(flinkClusterSessionTimeout);
        sb.append('}');
        return sb.toString();
    }

}
