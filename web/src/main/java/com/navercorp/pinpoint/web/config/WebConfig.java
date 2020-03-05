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

import javax.annotation.PostConstruct;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author koo.taejin
 */
@Configuration
public class WebConfig {

    private final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${cluster.enable:false}")
    private boolean clusterEnable;

    @Value("${cluster.web.tcp.port:0}")
    private int clusterTcpPort;

    @Value("${cluster.zookeeper.address:}")
    private String clusterZookeeperAddress;

    @Value("${cluster.zookeeper.sessiontimeout:-1}")
    private int clusterZookeeperSessionTimeout;

    @Value("${cluster.zookeeper.retry.interval:60000}")
    private int clusterZookeeperRetryInterval;

    @Value("${cluster.zookeeper.periodic.sync.enable:false}")
    private boolean clusterZookeeperPeriodicSyncEnable;

    @Value("${cluster.zookeeper.periodic.sync.interval:600000}")
    private int clusterZookeeperPeriodicSyncInterval;

    @Value("${cluster.connect.address:}")
    private String clusterConnectAddress;

    @PostConstruct
    public void validation() {
        if (isClusterEnable()) {
//            assertPort(clusterTcpPort);
            if (StringUtils.isEmpty(clusterZookeeperAddress)) {
                throw new IllegalArgumentException("clusterZookeeperAddress may not be empty =" + clusterZookeeperAddress);
            }
            assertPositiveNumber(clusterZookeeperSessionTimeout);
            assertPositiveNumber(clusterZookeeperRetryInterval);

            if (clusterZookeeperPeriodicSyncEnable) {
                assertPositiveNumber(clusterZookeeperPeriodicSyncInterval);
            }
        }

        logger.info("{}", this);
        AnnotationVisitor annotationVisitor = new AnnotationVisitor(Value.class);
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
        return clusterEnable;
    }

    public int getClusterTcpPort() {
        return clusterTcpPort;
    }

    public String getClusterZookeeperAddress() {
        return clusterZookeeperAddress;
    }

    public int getClusterZookeeperSessionTimeout() {
        return clusterZookeeperSessionTimeout;
    }

    public int getClusterZookeeperRetryInterval() {
        return clusterZookeeperRetryInterval;
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
                "clusterEnable=" + clusterEnable +
                ", clusterTcpPort=" + clusterTcpPort +
                ", clusterZookeeperAddress='" + clusterZookeeperAddress + '\'' +
                ", clusterZookeeperSessionTimeout=" + clusterZookeeperSessionTimeout +
                ", clusterZookeeperRetryInterval=" + clusterZookeeperRetryInterval +
                ", clusterZookeeperPeriodicSyncEnable=" + clusterZookeeperPeriodicSyncEnable +
                ", clusterZookeeperPeriodicSyncInterval=" + clusterZookeeperPeriodicSyncInterval +
                ", clusterConnectAddress='" + clusterConnectAddress + '\'' +
                '}';
    }

}
