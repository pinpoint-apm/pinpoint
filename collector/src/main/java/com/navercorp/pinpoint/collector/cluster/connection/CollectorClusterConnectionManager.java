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
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CollectorClusterConnectionManager implements  ClusterConnectionManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String clusterId;
    private final CollectorClusterConnectionRepository socketRepository;

    private final CollectorClusterConnector clusterConnector;
    private final CollectorClusterAcceptor clusterAcceptor;

    public CollectorClusterConnectionManager(String clusterId, CollectorClusterConnectionRepository socketRepository, CollectorClusterConnector client) {
        this(clusterId, socketRepository, client, null);
    }

    public CollectorClusterConnectionManager(String clusterId, CollectorClusterConnectionRepository socketRepository, CollectorClusterConnector client, CollectorClusterAcceptor acceptor) {
        this.clusterId = Objects.requireNonNull(clusterId, "clusterId");
        this.socketRepository = socketRepository;
        this.clusterConnector = Objects.requireNonNull(client, "clusterConnector");
        this.clusterAcceptor = acceptor;
    }

    @Override
    public void start() {
        logger.info("{} initialization started.", ClassUtils.simpleClassName(this));

        if (clusterConnector != null) {
            clusterConnector.start();
        }

        if (clusterAcceptor != null) {
            clusterAcceptor.start();
        }

        logger.info("{} initialization completed.", ClassUtils.simpleClassName(this));
    }

    @Override
    public void stop() {
        logger.info("{} destroying started.", ClassUtils.simpleClassName(this));

        for (PinpointSocket socket : socketRepository.getClusterSocketList()) {
            if (socket != null) {
                socket.close();
            }
        }

        if (clusterConnector != null) {
            clusterConnector.stop();
        }

        if (clusterAcceptor != null) {
            clusterAcceptor.stop();
        }

        logger.info("{} destroying completed.", ClassUtils.simpleClassName(this));
    }

    @Override
    public void connectPointIfAbsent(Address address) {
        logger.info("localhost -> {} connect started.", address);

        if (socketRepository.containsKey(address)) {
            logger.info("localhost -> {} already connected.", address);
            return;
        }

        PinpointSocket connect = clusterConnector.connect(address);
        socketRepository.putIfAbsent(address, connect);

        logger.info("localhost -> {} connect completed.", address);
    }

    @Override
    public void disconnectPoint(Address address) {
        logger.info("localhost -> {} disconnect started.", address);

        PinpointSocket socket = socketRepository.remove(address);
        if (socket != null) {
            socket.close();
            logger.info("localhost -> {} disconnect completed.", address);
        } else {
            logger.info("localhost -> {} already disconnected.", address);
        }
    }

    @Override
    public List<Address> getConnectedAddressList() {
        return socketRepository.getAddressList();
    }

}
