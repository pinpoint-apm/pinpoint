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

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.UnsupportOperationMessageListener;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.cluster.Role;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ClusterAcceptor implements ClusterConnectionProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String bindHost;
    private final int bindPort;

    private final PinpointServerAcceptor serverAcceptor;

    public ClusterAcceptor(String host, int port) {
        this.bindHost = host;
        this.bindPort = port;

        ClusterOption clusterOption = new ClusterOption(true, WebUtils.getServerIdentifier(), Role.CALLER);
        this.serverAcceptor = new PinpointServerAcceptor(clusterOption);
    }

    @Override
    public void start() {
        logger.info("{} initialization started.", ClassUtils.simpleClassName(this));

        this.serverAcceptor.setMessageListener(new WebClusterAcceptorListener());
        this.serverAcceptor.bind(new InetSocketAddress(bindHost, bindPort));

        logger.info("{} initialization completed.", ClassUtils.simpleClassName(this));
    }

    @Override
    public void stop() {
        logger.info("{} destroying started.", ClassUtils.simpleClassName(this));

        if (serverAcceptor != null) {
            serverAcceptor.close();
        }

        logger.info("{} destroying completed.", ClassUtils.simpleClassName(this));
    }

    public String getBindHost() {
        return bindHost;
    }

    public int getBindPort() {
        return bindPort;
    }

    @Override
    public List<PinpointSocket> getClusterSocketList() {
        return serverAcceptor.getWritableSocketList();
    }

    private class WebClusterAcceptorListener extends UnsupportOperationMessageListener implements ServerMessageListener {

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.warn("do handShake {}", properties);
            return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            logger.debug("ping received packet:{}, remote:{}", pingPacket, pinpointServer);
        }

    }

}
