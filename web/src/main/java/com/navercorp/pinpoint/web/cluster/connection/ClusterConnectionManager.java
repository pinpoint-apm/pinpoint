/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.cluster.connection;

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.web.config.WebConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ClusterConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebConfig config;

    private ClusterAcceptor clusterAcceptor;
    private ClusterConnector clusterConnector;

    public ClusterConnectionManager(WebConfig config) {
        this.config = config;
    }

    public void start() throws InterruptedException, IOException, KeeperException {
        logger.info("start() started.");

        int bindPort = config.getClusterTcpPort();
        if (bindPort > 0) {
            clusterAcceptor = new ClusterAcceptor(getRepresentationLocalV4Ip(), bindPort);
            clusterAcceptor.start();
        }

        final String clusterConnectAddress = config.getClusterConnectAddress();
        if (StringUtils.isNotBlank(clusterConnectAddress)) {
            clusterConnector = new ClusterConnector(clusterConnectAddress);
            clusterConnector.start();
        } else {
            logger.info("cluster.connect.address is empty");
        }

        logger.info("start() completed.");
    }

    public void stop() {
        logger.info("stop() started.");

        if (clusterConnector != null) {
            clusterConnector.stop();
            clusterConnector = null;
        }

        if (clusterAcceptor!= null) {
            clusterAcceptor.stop();
            clusterAcceptor = null;
        }

        logger.info("stop() completed.");
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

    public PinpointSocket getSocket(String clusterId) {
        if (clusterAcceptor != null) {
            List<PinpointSocket> clusterList = clusterAcceptor.getClusterSocketList();
            for (PinpointSocket cluster : clusterList) {
                ClusterOption remoteClusterOption = cluster.getRemoteClusterOption();
                if (remoteClusterOption != null) {
                    if (clusterId.equals(remoteClusterOption.getId())) {
                        return cluster;
                    }
                }
            }
        }

        if (clusterConnector != null) {
            List<PinpointSocket> clusterList = clusterConnector.getClusterSocketList();
            for (PinpointSocket cluster : clusterList) {
                ClusterOption remoteClusterOption = cluster.getRemoteClusterOption();
                if (remoteClusterOption != null) {
                    if (clusterId.equals(remoteClusterOption.getId())) {
                        return cluster;
                    }
                }
            }
        }

        return null;
    }

    public List<PinpointSocket> getClusterList() {
        List<PinpointSocket> clusterList = new ArrayList<>();

        if (clusterAcceptor != null) {
            clusterList.addAll(clusterAcceptor.getClusterSocketList());
        }

        if (clusterConnector != null) {
            clusterList.addAll(clusterConnector.getClusterSocketList());
        }

        return clusterList;
    }

    public ClusterAcceptor getClusterAcceptor() {
        return clusterAcceptor;
    }

    public ClusterConnector getClusterConnector() {
        return clusterConnector;
    }

}
