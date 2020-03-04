/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.cluster.connection;

import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.collector.util.DefaultAddress;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.cluster.Role;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerOption;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.ClassUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CollectorClusterAcceptor implements CollectorClusterConnectionProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String name;
    private final InetSocketAddress bindAddress;
    private final CollectorClusterConnectionRepository clusterSocketRepository;

    private PinpointServerAcceptor serverAcceptor;

    private final CollectorClusterConnectionOption option;

    public CollectorClusterAcceptor(CollectorClusterConnectionOption option, InetSocketAddress bindAddress, CollectorClusterConnectionRepository clusterSocketRepository) {
        this.name = ClassUtils.simpleClassName(this);
        this.option = Objects.requireNonNull(option, "option");
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.clusterSocketRepository = Objects.requireNonNull(clusterSocketRepository, "clusterSocketRepository");
    }

    @Override
    public void start() {
        logger.info("{} initialization started.", name);

        ClusterOption clusterOption = new ClusterOption(true, option.getClusterId(), Role.ROUTER);

        ServerOption.Builder serverOptionBuilder = new ServerOption.Builder();
        serverOptionBuilder.setClusterOption(clusterOption);

        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor(serverOptionBuilder.build(), ChannelFilter.BYPASS);
        serverAcceptor.setMessageListenerFactory(new ClusterServerMessageListenerFactory(option.getClusterId(), option.getRouteMessageHandler()));
        serverAcceptor.setServerStreamChannelMessageHandler(option.getRouteStreamMessageHandler());
        serverAcceptor.addStateChangeEventHandler(new WebClusterServerChannelStateChangeHandler());
        serverAcceptor.bind(bindAddress);

        this.serverAcceptor = serverAcceptor;

        logger.info("{} initialization completed.", name);
    }

    @Override
    public void stop() {
        logger.info("{} destroying started.", name);

        if (serverAcceptor != null) {
            serverAcceptor.close();
        }

        logger.info("{} destroying completed.", name);
    }

    class WebClusterServerChannelStateChangeHandler extends ServerStateChangeEventHandler {

        @Override
        public void stateUpdated(PinpointServer pinpointServer, SocketStateCode updatedStateCode) {
            if (updatedStateCode.isRunDuplex()) {
                Address address = getAddress(pinpointServer);
                clusterSocketRepository.putIfAbsent(address, pinpointServer);
            } else if (updatedStateCode.isClosed()) {
                Address address = getAddress(pinpointServer);
                clusterSocketRepository.remove(address);
            }
        }

        private Address getAddress(PinpointServer pinpointServer) {
            final SocketAddress remoteAddress = pinpointServer.getRemoteAddress();
            if (!(remoteAddress instanceof InetSocketAddress)) {
                throw new IllegalStateException("unexpected address type:" + remoteAddress);
            }
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
            return new DefaultAddress(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
        }

    }

}
