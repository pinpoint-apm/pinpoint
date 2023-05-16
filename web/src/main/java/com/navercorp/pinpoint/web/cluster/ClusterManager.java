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

package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.web.cluster.connection.ClusterAcceptor;
import com.navercorp.pinpoint.web.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.web.config.WebClusterProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ClusterManager {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WebClusterProperties properties;

    private final ClusterConnectionManager clusterConnectionManager;
    private final ClusterDataManager clusterDataManager;

    public ClusterManager(WebClusterProperties properties, ClusterConnectionManager clusterConnectionManager, ClusterDataManager clusterDataManager) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.clusterConnectionManager = Objects.requireNonNull(clusterConnectionManager, "clusterConnectionManager");
        this.clusterDataManager = Objects.requireNonNull(clusterDataManager, "clusterDataManager");
    }

    @PostConstruct
    public void start() {
        logger.info("start() started.");

        if (!properties.isClusterEnable()) {
            logger.info("start() skipped. caused:cluster option disabled.");
            return;
        }

        try {
            clusterConnectionManager.start();
            clusterDataManager.start();

            ClusterAcceptor clusterAcceptor = clusterConnectionManager.getClusterAcceptor();
            if (clusterAcceptor != null) {
                String nodeName = clusterAcceptor.getBindHost() + ":" + clusterAcceptor.getBindPort();
                List<String> localIpList = NetUtils.getLocalV4IpList();
                clusterDataManager.registerWebCluster(nodeName, convertIpListToBytes(localIpList, "\r\n"));
            }

        } catch (Exception e) {
            logger.warn("start() failed. caused:{}.", e.getMessage(), e);
            clearResource();
        }

        logger.info("start() completed.");
    }

    @PreDestroy
    public void stop() {
        logger.info("stop() started.");

        if (!properties.isClusterEnable()) {
            logger.info("stop() skipped. caused:cluster option disabled.");
            return;
        }

        clearResource();

        logger.info("stop() completed.");
    }

    private void clearResource() {
        if (clusterDataManager != null) {
            clusterDataManager.stop();
        }

        if (clusterConnectionManager != null) {
            clusterConnectionManager.stop();
        }
    }


    private String getRepresentationLocalV4Ip() {
        String ip = NetUtils.getLocalV4Ip();

        if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
            return ip;
        }

        // local ip addresses with all LOOPBACK addresses removed
        List<String> ipList = NetUtils.getLocalV4IpList();
        if (!ipList.isEmpty()) {
            return ipList.get(0);
        }

        return NetUtils.LOOPBACK_ADDRESS_V4;
    }

    private byte[] convertIpListToBytes(List<String> ipList, String delimiter) {
        String ipListStr = String.join(delimiter, ipList);
        return ipListStr.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isEnabled() {
        return properties.isClusterEnable();
    }

    public boolean isConnected(ClusterKey clusterKey) {
        if (!isEnabled()) {
            return false;
        }
        List<ClusterId> clusterIdList = clusterDataManager.getRegisteredAgentList(clusterKey);
        return clusterIdList.size() == 1;
    }


    public List<PinpointSocket> getSocket(ClusterKey clusterKey) {
        Objects.requireNonNull(clusterKey, "clusterKey");

        if (!isEnabled()) {
            return Collections.emptyList();
        }

        List<ClusterId> clusterIdList = clusterDataManager.getRegisteredAgentList(clusterKey);

        if (clusterIdList.isEmpty()) {
            logger.debug("{} couldn't find agent.", clusterKey);
            return Collections.emptyList();
        } else if (clusterIdList.size() > 1) {
            logger.debug("{} found duplicate agent {}.", clusterKey, clusterIdList);
        }

        List<PinpointSocket> pinpointSocketList = new ArrayList<>(clusterIdList.size());
        for (ClusterId clusterId : clusterIdList) {
            PinpointSocket pinpointSocket = clusterConnectionManager.getSocket(clusterId);
            if (pinpointSocket == null) {
                throw new IllegalStateException("clusterId not found " + clusterId);
            }
            pinpointSocketList.add(pinpointSocket);
        }

        return pinpointSocketList;
    }

}
