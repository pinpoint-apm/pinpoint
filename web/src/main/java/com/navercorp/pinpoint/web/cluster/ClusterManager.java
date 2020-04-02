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

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.web.cluster.connection.ClusterAcceptor;
import com.navercorp.pinpoint.web.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.web.config.WebConfig;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ClusterManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Charset charset = StandardCharsets.UTF_8;

    private final WebConfig config;

    private final ClusterConnectionManager clusterConnectionManager;
    private final ClusterDataManager clusterDataManager;

    public ClusterManager(WebConfig config, ClusterConnectionManager clusterConnectionManager, ClusterDataManager clusterDataManager) {
        this.config = Objects.requireNonNull(config, "config");
        this.clusterConnectionManager = Objects.requireNonNull(clusterConnectionManager, "clusterConnectionManager");
        this.clusterDataManager = Objects.requireNonNull(clusterDataManager, "clusterDataManager");
    }

    @PostConstruct
    public void start() throws InterruptedException, IOException, KeeperException {
        logger.info("start() started.");

        if (!config.isClusterEnable()) {
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

        if (!config.isClusterEnable()) {
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

        List<String> clusterIdList = clusterDataManager.getRegisteredAgentList(agentInfo);
        return clusterIdList.size() == 1;
    }

    public PinpointSocket getSocket(AgentInfo agentInfo) {
        return getSocket(agentInfo.getApplicationName(), agentInfo.getAgentId(), agentInfo.getStartTimestamp());
    }

    public PinpointSocket getSocket(String applicationName, String agentId, long startTimeStamp) {
        if (!config.isClusterEnable()) {
            return null;
        }

        List<String> clusterIdList = clusterDataManager.getRegisteredAgentList(applicationName, agentId, startTimeStamp);

        // having duplicate AgentName registered is an exceptional case
        if (clusterIdList.isEmpty()) {
            logger.warn("{}/{}/{} couldn't find agent.", applicationName, agentId, startTimeStamp);
            return null;
        } else if (clusterIdList.size() > 1) {
            logger.warn("{}/{}/{} found duplicate agent {}.", applicationName, agentId, startTimeStamp, clusterIdList);
            return null;
        }

        String clusterId = clusterIdList.get(0);
        return clusterConnectionManager.getSocket(clusterId);
    }

}
