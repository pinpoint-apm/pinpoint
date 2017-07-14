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

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.cluster.Role;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class CollectorClusterConnector implements CollectorClusterConnectionProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CollectorClusterConnectionOption option;

    private PinpointClientFactory clientFactory;
    public CollectorClusterConnector(CollectorClusterConnectionOption option) {
        this.option = option;
    }

    @Override
    public void start() {
        logger.info("{} initialization started.", ClassUtils.simpleClassName(this));

        ClusterOption clusterOption = new ClusterOption(true, option.getClusterId(), Role.ROUTER);

        this.clientFactory = new DefaultPinpointClientFactory();

        this.clientFactory.setTimeoutMillis(1000 * 5);
        this.clientFactory.setMessageListener(option.getRouteMessageHandler());
        this.clientFactory.setServerStreamChannelMessageListener(option.getRouteStreamMessageHandler());
        this.clientFactory.setClusterOption(clusterOption);

        Map<String, Object> properties = new HashMap<>();
        properties.put("id", option.getClusterId());
        clientFactory.setProperties(properties);

        logger.info("{} initialization completed.", ClassUtils.simpleClassName(this));
    }

    @Override
    public void stop() {
        logger.info("{} destroying started.", ClassUtils.simpleClassName(this));

        if (clientFactory != null) {
            clientFactory.release();
        }

        logger.info("{} destroying completed.", ClassUtils.simpleClassName(this));
    }

    PinpointSocket connect(InetSocketAddress address) {
        if (clientFactory == null) {
            throw new IllegalStateException("not started.");
        }

        PinpointSocket socket = ClientFactoryUtils.createPinpointClient(address, clientFactory);
        return socket;
    }

}
