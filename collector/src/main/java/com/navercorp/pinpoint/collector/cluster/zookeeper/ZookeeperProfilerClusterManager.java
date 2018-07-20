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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRepository;
import com.navercorp.pinpoint.collector.cluster.PinpointServerClusterPoint;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ZookeeperProfilerClusterManager implements ServerStateChangeEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZookeeperJobWorker worker;

    private final CommonStateContext workerState;

    private final ClusterPointRepository profileCluster;

    private final Object lock = new Object();

    // keep it simple - register on RUN, remove on FINISHED, skip otherwise
    // should only be instantiated when cluster is enabled.
    public ZookeeperProfilerClusterManager(ZookeeperClient client, String serverIdentifier, ClusterPointRepository profileCluster) {
        this.workerState = new CommonStateContext();
        this.profileCluster = profileCluster;

        this.worker = new ZookeeperJobWorker(client, serverIdentifier);
    }

    public void start() {
        switch (this.workerState.getCurrentState()) {
            case NEW:
                if (this.workerState.changeStateInitializing()) {
                    logger.info("start() started.");

                    worker.start();
                    workerState.changeStateStarted();

                    logger.info("start() completed.");

                    break;
                }
            case INITIALIZING:
                logger.info("start() failed. caused:already initializing.");
                break;
            case STARTED:
                logger.info("start() failed. caused:already started.");
                break;
            case DESTROYING:
                throw new IllegalStateException("Already destroying.");
            case STOPPED:
                throw new IllegalStateException("Already stopped.");
            case ILLEGAL_STATE:
                throw new IllegalStateException("Invalid State.");
        }
    }

    public void stop() {
        if (!(this.workerState.changeStateDestroying())) {
            logger.info("stop() failed. caused:unexpected state.");
            return;
        }

        logger.info("stop() started.");

        worker.stop();
        this.workerState.changeStateStopped();

        logger.info("stop() completed.");
    }

    @Override
    public void eventPerformed(PinpointServer pinpointServer, SocketStateCode stateCode) {
        if (workerState.isStarted()) {
            logger.info("eventPerformed() started. (PinpointServer={}, State={})", pinpointServer, stateCode);

            Map agentProperties = pinpointServer.getChannelProperties();

            // skip when applicationName and agentId is unknown
            if (skipAgent(agentProperties)) {
                return;
            }

            synchronized (lock) {
                if (SocketStateCode.RUN_DUPLEX == stateCode) {
                    profileCluster.addClusterPoint(new PinpointServerClusterPoint(pinpointServer));
                    worker.addPinpointServer(pinpointServer);
                } else if (SocketStateCode.isClosed(stateCode)) {
                    profileCluster.removeClusterPoint(new PinpointServerClusterPoint(pinpointServer));
                    worker.removePinpointServer(pinpointServer);
                }
            }
        } else {
            logger.info("eventPerformed() failed. caused:unexpected state.");
        }
    }
    
    @Override
    public void exceptionCaught(PinpointServer pinpointServer, SocketStateCode stateCode, Throwable e) {
        logger.warn("exceptionCaught(). (pinpointServer:{}, PinpointServerStateCode:{}). caused:{}.", pinpointServer, stateCode, e.getMessage(), e);
    }

    public List<String> getClusterData() {
        return worker.getClusterList();
    }

    public void initZookeeperClusterData() {
        worker.clear();

        synchronized (lock) {
            List clusterPointList = profileCluster.getClusterPointList();
            for (Object clusterPoint : clusterPointList) {
                if (clusterPoint instanceof PinpointServerClusterPoint) {
                    PinpointServer pinpointServer = ((PinpointServerClusterPoint) clusterPoint).getPinpointServer();
                    if (SocketStateCode.isRunDuplex(pinpointServer.getCurrentStateCode())) {
                        worker.addPinpointServer(pinpointServer);
                    }
                }
            }
        }
    }

    private boolean skipAgent(Map<Object, Object> agentProperties) {
        String applicationName = MapUtils.getString(agentProperties, HandshakePropertyType.APPLICATION_NAME.getName());
        String agentId = MapUtils.getString(agentProperties, HandshakePropertyType.AGENT_ID.getName());

        if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId)) {
            return true;
        }

        return false;
    }

}
