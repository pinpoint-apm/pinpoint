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

package com.navercorp.pinpoint.collector.cluster;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;

/**
 * @author koo.taejin
 */
public class WebCluster implements Cluster {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PinpointClientFactory clientFactory;

    private final Map<InetSocketAddress, PinpointClient> clusterRepository = new HashMap<InetSocketAddress, PinpointClient>();

    public WebCluster(String id, MessageListener messageListener) {
        this(id, messageListener, DisabledServerStreamChannelMessageListener.INSTANCE);
    }

    public WebCluster(String id, MessageListener messageListener, ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
        this.clientFactory = new PinpointClientFactory();
        this.clientFactory.setTimeoutMillis(1000 * 5);
        this.clientFactory.setMessageListener(messageListener);
        this.clientFactory.setServerStreamChannelMessageListener(serverStreamChannelMessageListener);
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("id", id);

        clientFactory.setProperties(properties);
    }

    // Not safe for use by multiple threads.
    public void connectPointIfAbsent(InetSocketAddress address) {
        logger.info("localhost -> {} connect started.", address);

        if (clusterRepository.containsKey(address)) {
            logger.info("localhost -> {} already connected.", address);
            return;
        }

        PinpointClient client = createPinpointClient(address);
        clusterRepository.put(address, client);

        logger.info("localhost -> {} connect completed.", address);
    }

    // Not safe for use by multiple threads.
    public void disconnectPoint(InetSocketAddress address) {
        logger.info("localhost -> {} disconnect started.", address);

        PinpointClient socket = clusterRepository.remove(address);
        if (socket != null) {
            socket.close();
            logger.info("localhost -> {} disconnect completed.", address);
        } else {
            logger.info("localhost -> {} already disconnected.", address);
        }
    }

    private PinpointClient createPinpointClient(InetSocketAddress address) {
        String host = address.getHostName();
        int port = address.getPort();

        PinpointClient client = null;
        for (int i = 0; i < 3; i++) {
            try {
                client = clientFactory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return client;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        client = clientFactory.scheduledConnect(host, port);

        return client;
    }

    public List<InetSocketAddress> getWebClusterList() {
        return new ArrayList<InetSocketAddress>(clusterRepository.keySet());
    }

    public void close() {
        for (PinpointClient client : clusterRepository.values()) {
            if (client != null) {
                client.close();
            }
        }

        if (clientFactory != null) {
            clientFactory.release();
        }
    }

}
