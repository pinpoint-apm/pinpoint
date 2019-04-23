/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperProfilerClusterManager;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.MapUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ClusterPointStateChangedEventHandler extends ServerStateChangeEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZookeeperProfilerClusterManager zookeeperProfilerClusterManager;

    public ClusterPointStateChangedEventHandler(ZookeeperProfilerClusterManager zookeeperProfilerClusterManager) {
        this.zookeeperProfilerClusterManager = Assert.requireNonNull(zookeeperProfilerClusterManager, "zookeeperProfilerClusterManager must not be null");
    }

    @Override
    public void stateUpdated(PinpointServer pinpointServer, SocketStateCode updatedStateCode) {
        logger.info("stateUpdated() started. (PinpointServer={}, updatedStateCode={})", pinpointServer, updatedStateCode);

        Map agentProperties = pinpointServer.getChannelProperties();
        // skip when applicationName and agentId is unknown
        if (skipAgent(agentProperties)) {
            return;
        }

        if (SocketStateCode.RUN_DUPLEX == updatedStateCode) {
            ThriftAgentConnection pinpointServerClusterPoint = new ThriftAgentConnection(pinpointServer);
            zookeeperProfilerClusterManager.register(pinpointServerClusterPoint);
        } else if (SocketStateCode.isClosed(updatedStateCode)) {
            ThriftAgentConnection pinpointServerClusterPoint = new ThriftAgentConnection(pinpointServer);
            zookeeperProfilerClusterManager.unregister(pinpointServerClusterPoint);
        }
    }

    private boolean skipAgent(Map<Object, Object> agentProperties) {
        String applicationName = MapUtils.getString(agentProperties, HandshakePropertyType.APPLICATION_NAME.getName());
        String agentId = MapUtils.getString(agentProperties, HandshakePropertyType.AGENT_ID.getName());

        if (StringUtils.hasText(applicationName) && StringUtils.hasText(agentId)) {
            return false;
        }
        return true;
    }

}
