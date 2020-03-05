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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ClusterPointStateChangedEventHandler extends ServerStateChangeEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerClusterManager profilerClusterManager;
    private final ChannelPropertiesFactory channelPropertiesFactory;

    public ClusterPointStateChangedEventHandler(ChannelPropertiesFactory channelPropertiesFactory, ProfilerClusterManager profilerClusterManager) {
        this.channelPropertiesFactory = Objects.requireNonNull(channelPropertiesFactory, "channelPropertiesFactory");
        this.profilerClusterManager = Objects.requireNonNull(profilerClusterManager, "profilerClusterManager");
    }

    @Override
    public void stateUpdated(PinpointServer pinpointServer, SocketStateCode updatedStateCode) {
        logger.info("stateUpdated() started. (PinpointServer={}, updatedStateCode={})", pinpointServer, updatedStateCode);

        Map<Object, Object> channelPropertiesMap = pinpointServer.getChannelProperties();
        ChannelProperties channelProperties = channelPropertiesFactory.newChannelProperties(channelPropertiesMap);
        // skip when applicationName and agentId is unknown
        if (skipAgent(channelProperties)) {
            return;
        }

        if (SocketStateCode.RUN_DUPLEX == updatedStateCode) {
            ClusterPoint<byte[]> pinpointServerClusterPoint = newClusterPoint(pinpointServer, channelProperties);
            profilerClusterManager.register(pinpointServerClusterPoint);
        } else if (SocketStateCode.isClosed(updatedStateCode)) {
            ClusterPoint<byte[]> pinpointServerClusterPoint = newClusterPoint(pinpointServer, channelProperties);
            profilerClusterManager.unregister(pinpointServerClusterPoint);
        }
    }

    private ClusterPoint<byte[]> newClusterPoint(PinpointServer pinpointServer, ChannelProperties channelProperties) {
        return ThriftAgentConnection.newClusterPoint(pinpointServer, channelProperties);
    }

    private boolean skipAgent(ChannelProperties channelProperties) {
        if (channelProperties == null) {
            return true;
        }
        String applicationName = channelProperties.getApplicationName();
        String agentId = channelProperties.getAgentId();

        if (StringUtils.hasText(applicationName) && StringUtils.hasText(agentId)) {
            return false;
        }
        return true;
    }

}
