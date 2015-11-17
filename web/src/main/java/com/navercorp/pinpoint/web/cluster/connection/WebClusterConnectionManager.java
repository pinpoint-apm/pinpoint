/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.cluster.connection;

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.web.cluster.ClusterManager;
import com.navercorp.pinpoint.web.cluster.zookeeper.ZookeeperClusterManager;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author Taejin Koo
 */
public class WebClusterConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Charset charset = Charset.forName("UTF-8");

    private final WebConfig config;

    private final WebClusterAcceptor clusterAcceptor;
    private final WebClusterConnector clusterConnector;

    private ClusterManager clusterManager;

    public WebClusterConnectionManager(WebConfig config) {
        this.config = config;

        if (config.isClusterEnable()) {
            int bindPort = config.getClusterTcpPort();
            if (bindPort > 0) {
                clusterAcceptor = new WebClusterAcceptor(getRepresentationLocalV4Ip(), bindPort);
            } else {
                clusterAcceptor = null;
            }

            String connectAddress = config.getClusterConnectAddress();
            if (connectAddress != null && connectAddress.trim().length() != 0) {
                clusterConnector = new WebClusterConnector(config.getClusterConnectAddress());
            } else {
                clusterConnector = null;
            }
        } else {
            clusterAcceptor = null;
            clusterConnector = null;
        }
    }

    @PostConstruct
    public void start() throws InterruptedException, IOException, KeeperException {
        if (!config.isClusterEnable()) {
            return;
        }

        logger.info("{} initialization started.", ClassUtils.simpleClassName(this));
        this.clusterManager = new ZookeeperClusterManager(config.getClusterZookeeperAddress(), config.getClusterZookeeperSessionTimeout(), config.getClusterZookeeperRetryInterval());

        if (clusterConnector != null) {
            clusterConnector.start();
        }

        if (clusterAcceptor != null) {
            clusterAcceptor.start();

            // TODO need modification - storing ip list using \r\n as delimiter since json list is not supported natively
            String nodeName = clusterAcceptor.getBindHost() + ":" + clusterAcceptor.getBindPort();
            List<String> localIpList = NetUtils.getLocalV4IpList();
            this.clusterManager.registerWebCluster(nodeName, convertIpListToBytes(localIpList, "\r\n"));
        }

        logger.info("{} initialization completed.", ClassUtils.simpleClassName(this));
    }

    @PreDestroy
    public void stop() {
        logger.info("{} destroying started.", ClassUtils.simpleClassName(this));

        if (clusterManager != null) {
            clusterManager.close();
        }

        if (clusterConnector != null) {
            clusterConnector.stop();
        }

        if (clusterAcceptor!= null) {
            clusterAcceptor.stop();
        }

        logger.info("{} destroying completed.", ClassUtils.simpleClassName(this));
    }

    private String getRepresentationLocalV4Ip() {
        String ip = NetUtils.getLocalV4Ip();

        if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
            return ip;
        }

        // local ip addresses with all LOOPBACK addresses removed
        List<String> ipList = NetUtils.getLocalV4IpList();
        if (ipList.size() > 0) {
            return ipList.get(0);
        }

        return NetUtils.LOOPBACK_ADDRESS_V4;
    }

    private byte[] convertIpListToBytes(List<String> ipList, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<String> ipIterator = ipList.iterator();
        while (ipIterator.hasNext()) {
            String eachIp = ipIterator.next();
            stringBuilder.append(eachIp);

            if (ipIterator.hasNext()) {
                stringBuilder.append(delimiter);
            }
        }

        return stringBuilder.toString().getBytes(charset);
    }

    public boolean isConnected(AgentInfo agentInfo) {
        if (!config.isClusterEnable()) {
            return false;
        }

        List<String> clusterIdList = clusterManager.getRegisteredAgentList(agentInfo);
        return clusterIdList.size() == 1;
    }

    public PinpointSocket getSocket(AgentInfo agentInfo) {
        return getSocket(agentInfo.getApplicationName(), agentInfo.getAgentId(), agentInfo.getStartTimestamp());
    }

    public PinpointSocket getSocket(String applicationName, String agentId, long startTimeStamp) {
        if (!config.isClusterEnable()) {
            return null;
        }

        List<String> clusterIdList = clusterManager.getRegisteredAgentList(applicationName, agentId, startTimeStamp);

        // having duplicate AgentName registered is an exceptional case
        if (clusterIdList.size() == 0) {
            logger.warn("{}/{}/{} couldn't find agent.", applicationName, agentId, startTimeStamp);
            return null;
        } else if (clusterIdList.size() > 1) {
            logger.warn("{}/{}/{} found duplicate agent {}.", applicationName, agentId, startTimeStamp, clusterIdList);
            return null;
        }

        String clusterId = clusterIdList.get(0);
        return getSocket0(clusterId);
    }

    private PinpointSocket getSocket0(String clusterId) {
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

}
