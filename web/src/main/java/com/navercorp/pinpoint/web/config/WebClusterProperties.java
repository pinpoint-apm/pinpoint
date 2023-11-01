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

package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterProperties;
import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author koo.taejin
 */
public class WebClusterProperties {

    private final Logger logger = LogManager.getLogger(getClass());

    @Qualifier("clusterProperties")
    @Autowired
    private ZookeeperClusterProperties clusterProperties;

    @Value("${cluster.web.tcp.hostaddress:}")
    private String hostAddress;

    @Value("${cluster.web.tcp.port:0}")
    private int clusterTcpPort;

    @Value("${cluster.zookeeper.retry.interval:60000}")
    private int clusterZookeeperRetryInterval;

    @Value("${cluster.zookeeper.periodic.sync.enable:false}")
    private boolean clusterZookeeperPeriodicSyncEnable;

    @Value("${cluster.zookeeper.periodic.sync.interval:600000}")
    private int clusterZookeeperPeriodicSyncInterval;

    @Value("${cluster.zookeeper.pull-retry.interval:15000}")
    private int pullRetryIntervalTimeMillis;

    @Value("${cluster.connect.address:}")
    private String clusterConnectAddress;

    @PostConstruct
    public void validation() {
        if (isClusterEnable()) {
//            assertPort(clusterTcpPort);
            final String zookeeperAddress = clusterProperties.getAddress();
            if (StringUtils.isEmpty(zookeeperAddress)) {
                throw new IllegalArgumentException("clusterZookeeperAddress may not be empty =" + zookeeperAddress);
            }
            assertPositiveNumber(clusterZookeeperRetryInterval);

            if (clusterZookeeperPeriodicSyncEnable) {
                assertPositiveNumber(clusterZookeeperPeriodicSyncInterval);
            }
        }

        logger.info("{}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    private int assertPort(int port) {
        if (port > 0 && 65535 > port) {
            return port;
        }

        throw new IllegalArgumentException("Invalid Port =" + port);
    }

    private int assertPositiveNumber(int number) {
        if (number >= 0) {
            return number;
        }

        throw new IllegalArgumentException("Invalid Positive Number =" + number);
    }

    public boolean isClusterEnable() {
        return clusterProperties.isEnable();
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getClusterTcpPort() {
        return clusterTcpPort;
    }

    public String getClusterZookeeperAddress() {
        return clusterProperties.getAddress();
    }

    public String getWebZNodePath() {
        return clusterProperties.getWebZNodePath();
    }

    public String getCollectorZNodePath() {
        return clusterProperties.getCollectorZNodePath();
    }

    public int getClusterZookeeperSessionTimeout() {
        return clusterProperties.getSessionTimeout();
    }

    public int getClusterZookeeperRetryInterval() {
        return clusterZookeeperRetryInterval;
    }

    public int getPullRetryIntervalTimeMillis() {
        return pullRetryIntervalTimeMillis;
    }

    public boolean isClusterZookeeperPeriodicSyncEnable() {
        return clusterZookeeperPeriodicSyncEnable;
    }

    public int getClusterZookeeperPeriodicSyncInterval() {
        return clusterZookeeperPeriodicSyncInterval;
    }

    public String getClusterConnectAddress() {
        return clusterConnectAddress;
    }

    @Override
    public String toString() {
        return "WebConfig{" +
                "clusterEnable=" + isClusterEnable() +
                ", clusterTcpPort=" + clusterTcpPort +
                ", clusterZookeeperAddress='" + getClusterZookeeperAddress() + '\'' +
                ", webZNodePath='" + getWebZNodePath() + '\'' +
                ", collectorZNodePath='" + getCollectorZNodePath() + '\'' +
                ", clusterZookeeperSessionTimeout=" + getClusterZookeeperSessionTimeout() +
                ", clusterZookeeperRetryInterval=" + clusterZookeeperRetryInterval +
                ", clusterZookeeperPeriodicSyncEnable=" + clusterZookeeperPeriodicSyncEnable +
                ", clusterZookeeperPeriodicSyncInterval=" + clusterZookeeperPeriodicSyncInterval +
                ", clusterConnectAddress='" + clusterConnectAddress + '\'' +
                '}';
    }

}
