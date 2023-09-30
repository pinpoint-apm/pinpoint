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
package com.navercorp.pinpoint.collector.flink;

import com.navercorp.pinpoint.collector.sender.FlinkRequestFactory;
import com.navercorp.pinpoint.collector.sender.FlinkTcpDataSender;
import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.sender.TcpDataSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkClusterConnectionManager implements ClusterConnectionManager {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final PinpointClientFactory pinpointClientFactory;
    private final TcpDataSenderRepository tcpDataSenderRepository;
    private final SerializerFactory<HeaderTBaseSerializer> flinkHeaderTBaseSerializerFactory;
    private final FlinkRequestFactory flinkRequestFactory;

    public FlinkClusterConnectionManager(TcpDataSenderRepository tcpDataSenderRepository,
                                         SerializerFactory<HeaderTBaseSerializer> flinkHeaderTBaseSerializerFactory,
                                         FlinkRequestFactory flinkRequestFactory) {
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
            HeaderTBaseSerializer serializer = flinkHeaderTBaseSerializerFactory.createSerializer();
            TcpDataSender<TBase<?, ?>> tcpDataSender = new FlinkTcpDataSender("flink", host, port, pinpointClientFactory, serializer, flinkRequestFactory);
            return new SenderContext(tcpDataSender);
        } catch (Exception e) {
            logger.error("not create tcpDataSender for {}.", address, e);
        }

        return null;
    }
}
