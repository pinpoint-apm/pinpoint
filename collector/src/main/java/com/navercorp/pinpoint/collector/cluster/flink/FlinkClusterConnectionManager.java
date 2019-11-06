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
package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.collector.sender.FlinkRequestFactory;
import com.navercorp.pinpoint.collector.sender.FlinkTcpDataSender;
import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkClusterConnectionManager implements ClusterConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PinpointClientFactory pinpointClientFactory;
    private final TcpDataSenderRepository tcpDataSenderRepository;
    private final FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory;
    private final FlinkRequestFactory flinkRequestFactory;

    public FlinkClusterConnectionManager(TcpDataSenderRepository tcpDataSenderRepository, FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory, FlinkRequestFactory flinkRequestFactory) {
        this.tcpDataSenderRepository = Objects.requireNonNull(tcpDataSenderRepository, "tcpDataSenderRepository");
        this.flinkHeaderTBaseSerializerFactory = Objects.requireNonNull(flinkHeaderTBaseSerializerFactory, "flinkHeaderTBaseSerializerFactory");
        this.flinkRequestFactory = Objects.requireNonNull(flinkRequestFactory, "flinkRequestFactory");
        this.pinpointClientFactory = newPointClientFactory();
    }

    private PinpointClientFactory newPointClientFactory() {
        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory();
        pinpointClientFactory.setWriteTimeoutMillis(1000 * 3);
        pinpointClientFactory.setRequestTimeoutMillis(1000 * 5);
        return pinpointClientFactory;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        for (SenderContext senderContext : tcpDataSenderRepository.getClusterSocketList()) {
            senderContext.close();
        }
        logger.info("{} stop completed.", this.getClass().getSimpleName());
    }

    @Override
    public void connectPointIfAbsent(Address address) {
        logger.info("localhost -> {} connect started.", address);

        if (tcpDataSenderRepository.containsKey(address)) {
            logger.info("localhost -> {} already connected.", address);
            return;
        }

        final SenderContext senderContext = createTcpDataSender(address);
        if (senderContext == null) {
            return;
        }

        final SenderContext context = tcpDataSenderRepository.putIfAbsent(address, senderContext);
        if (context != null) {
            logger.info("FlinkTcpDataSender have already been for {}.", address);
            senderContext.close();
        }

        logger.info("localhost -> {} connect completed.", address);
    }

    @Override
    public void disconnectPoint(Address address) {
        logger.info("localhost -> {} disconnect started.", address);

        final SenderContext context = tcpDataSenderRepository.remove(address);
        if (context != null) {
            context.close();
            logger.info("localhost -> {} disconnect completed.", address);
        } else {
            logger.info("localhost -> {} already disconnected.", address);
        }
    }

    @Override
    public List<Address> getConnectedAddressList() {
        return tcpDataSenderRepository.getAddressList();
    }

    private SenderContext createTcpDataSender(Address address) {
        try {
            final String host = address.getHost();
            final int port = address.getPort();
            FlinkHeaderTBaseSerializer serializer = flinkHeaderTBaseSerializerFactory.createSerializer();
            FlinkTcpDataSender tcpDataSender = new FlinkTcpDataSender("flink", host, port, pinpointClientFactory, serializer, flinkRequestFactory);
            return new SenderContext(tcpDataSender);
        } catch (Exception e) {
            logger.error("not create tcpDataSender for {}.", address, e);
        }

        return null;
    }
}
