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
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class FlinkClusterConnectionManager implements ClusterConnectionManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DefaultPinpointClientFactory pinpointClientFactory;
    private final TcpDataSenderRepository tcpDataSenderRepository;
    private final FlinkHeaderTBaseSerializerFactory flinkHeaderTBaseSerializerFactory;

    public FlinkClusterConnectionManager(TcpDataSenderRepository tcpDataSenderRepository) {
        this.tcpDataSenderRepository = tcpDataSenderRepository;
        this.pinpointClientFactory = new DefaultPinpointClientFactory();
        this.pinpointClientFactory.setTimeoutMillis(1000 * 5);
        this.flinkHeaderTBaseSerializerFactory = new FlinkHeaderTBaseSerializerFactory();
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
    public void connectPointIfAbsent(InetSocketAddress address) {
        logger.info("localhost -> {} connect started.", address);

        if (tcpDataSenderRepository.containsKey(address)) {
            logger.info("localhost -> {} already connected.", address);
            return;
        }

        SenderContext senderContext = createTcpDataSender(address);

        if (senderContext == null) {
            return;
        }

        SenderContext context = tcpDataSenderRepository.putIfAbsent(address, senderContext);

        if (context != null) {
            logger.info("TcpDataSender have already been for {}.", address);
            senderContext.close();
        }

        logger.info("localhost -> {} connect completed.", address);
    }

    @Override
    public void disconnectPoint(SocketAddress address) {
        logger.info("localhost -> {} disconnect started.", address);

        SenderContext context = tcpDataSenderRepository.remove(address);
        if (context != null) {
            context.close();
            logger.info("localhost -> {} disconnect completed.", address);
        } else {
            logger.info("localhost -> {} already disconnected.", address);
        }
    }

    @Override
    public List<SocketAddress> getConnectedAddressList() {
        return tcpDataSenderRepository.getAddressList();
    }

    private SenderContext createTcpDataSender(InetSocketAddress address) {
        PinpointClient client = null;
        try {
            client = ClientFactoryUtils.createPinpointClient(address, pinpointClientFactory);
            TcpDataSender tcpDataSender = new TcpDataSender(client, flinkHeaderTBaseSerializerFactory.createSerializer());
            return new SenderContext(tcpDataSender, client);
        } catch (Exception e) {
            logger.error("not create tcpDataSender for {}.", address, e);

            if (client != null) {
                client.close();
            }
        }

        return null;
    }
}
