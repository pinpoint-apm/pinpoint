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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author koo.taejin
 */
public class WebConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{pinpointWebProps['cluster.enable'] ?: false}")
    private boolean clusterEnable;

    @Value("#{pinpointWebProps['cluster.web.tcp.port'] ?: 0}")
    private int clusterTcpPort;

    @Value("#{pinpointWebProps['cluster.zookeeper.address'] ?: ''}")
    private String clusterZookeeperAddress;

    @Value("#{pinpointWebProps['cluster.zookeeper.sessiontimeout'] ?: -1}")
    private int clusterZookeeperSessionTimeout;

    @Value("#{pinpointWebProps['cluster.zookeeper.retry.interval'] ?: 60000}")
    private int clusterZookeeperRetryInterval;

    @Value("#{pinpointWebProps['cluster.connect.address'] ?: ''}")
    private String clusterConnectAddress;

    @PostConstruct
    public void validation() {
        if (isClusterEnable()) {
//            assertPort(clusterTcpPort);
            if(StringUtils.isEmpty(clusterZookeeperAddress)) {
                throw new IllegalArgumentException("clusterZookeeperAddress may not be empty =" + clusterZookeeperAddress);
            }
            assertPositiveNumber(clusterZookeeperSessionTimeout);
            assertPositiveNumber(clusterZookeeperRetryInterval);
        }

        logger.info("{}", toString());
    }

    private boolean assertPort(int port) {
        if (port > 0 && 65535 > port) {
            return true;
        }

        throw new IllegalArgumentException("Invalid Port =" + port);
    }

    private boolean assertPositiveNumber(int number) {
        if (number >= 0) {
            return true;
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

    public String getClusterConnectAddress() {
        return clusterConnectAddress;
    }

    @Override
    public String toString() {
        return "WebConfig [clusterEnable=" + clusterEnable
                + ", clusterTcpPort=" + clusterTcpPort
                + ", clusterZookeeperAddress=" + clusterZookeeperAddress
                + ", clusterZookeeperSessionTimeout=" + clusterZookeeperSessionTimeout
                + ", clusterConnectAddress=" + clusterConnectAddress + "]";
    }

    public int getClusterZookeeperRetryInterval() {
        return clusterZookeeperRetryInterval;
    }

    public void setClusterZookeeperRetryInterval(int clusterZookeeperRetryInterval) {
        this.clusterZookeeperRetryInterval = clusterZookeeperRetryInterval;
    }

}
