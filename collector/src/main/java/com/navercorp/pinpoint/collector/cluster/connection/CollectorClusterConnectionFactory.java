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

import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public class CollectorClusterConnectionFactory implements CollectorClusterConnectionOption {

    private final String clusterId;

    private final MessageListener routeMessageHandler;

    private final ServerStreamChannelMessageListener routeStreamMessageHandler;

    public CollectorClusterConnectionFactory(String clusterId, MessageListener routeMessageHandler, ServerStreamChannelMessageListener routeStreamMessageHandler) {
        this.clusterId = clusterId;
        this.routeMessageHandler = routeMessageHandler;
        this.routeStreamMessageHandler = routeStreamMessageHandler;
    }

    public CollectorClusterConnector createConnector() {
        return new CollectorClusterConnector(this);
    }

    public CollectorClusterAcceptor createAcceptor(InetSocketAddress bindAddress, CollectorClusterConnectionRepository clusterSocketRepository) {
        return new CollectorClusterAcceptor(this, bindAddress, clusterSocketRepository);
    }

    @Override
    public String getClusterId() {
        return clusterId;
    }

    @Override
    public MessageListener getRouteMessageHandler() {
        return routeMessageHandler;
    }

    @Override
    public ServerStreamChannelMessageListener getRouteStreamMessageHandler() {
        return routeStreamMessageHandler;
    }

}
