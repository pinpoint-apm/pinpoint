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

package com.navercorp.pinpoint.collector.receiver;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.config.DataReceiverGroupConfiguration;
import com.navercorp.pinpoint.collector.receiver.tcp.TCPReceiver;
import com.navercorp.pinpoint.collector.receiver.udp.BaseUDPHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.udp.NetworkAvailabilityCheckPacketFilter;
import com.navercorp.pinpoint.collector.receiver.udp.PacketHandlerFactory;
import com.navercorp.pinpoint.collector.receiver.udp.TBaseFilterChain;
import com.navercorp.pinpoint.collector.receiver.udp.UDPReceiver;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DataReceiverGroup can have a TCP Receiver and a UDP Receiver with the same behavior
 *
 * @author Taejin Koo
 */
public class DataReceiverGroup implements DataReceiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DispatchWorker worker;
    private final List<DataReceiver> receiverList;

    @Autowired
    private MetricRegistry metricRegistry;

    public DataReceiverGroup(String name, DataReceiverGroupConfiguration configuration, AddressFilter ignoreAddressFilter, DispatchHandler dispatchHandler) {
        Objects.requireNonNull(configuration, "configuration must not be null");
        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");

        String workerName = String.format("Pinpoint-%s-Worker", name);
        DispatchWorkerOption dispatchWorkerOption = new DispatchWorkerOption(workerName, configuration.getWorkerThreadSize(), configuration.getWorkerQueueSize(), 100, configuration.isWorkerMonitorEnable());
        this.worker = new DispatchWorker(dispatchWorkerOption);

        List<DataReceiver> receiverList = new ArrayList<>();

        if (configuration.isUdpEnable()) {
            DataReceiver udpReceiver = createUdpReceiver(name, configuration, dispatchHandler, ignoreAddressFilter);
            receiverList.add(udpReceiver);
        }

        if (configuration.isTcpEnable()) {
            DataReceiver tcpReceiver = createTcpReceiver(name, configuration, dispatchHandler, ignoreAddressFilter);
            receiverList.add(tcpReceiver);
        }

        Assert.isTrue(receiverList.size() > 0, "receiver must be greater than 0");

        this.receiverList = Collections.unmodifiableList(receiverList);
    }

    private UDPReceiver createUdpReceiver(String name, DataReceiverGroupConfiguration configuration, DispatchHandler dispatchHandler, AddressFilter ignoreAddressFilter) {
        String udpReceiverName = String.format("Pinpoint-UDP-%s-Receiver", name);
        TBaseFilterChain filterChain = new TBaseFilterChain(Arrays.asList(new NetworkAvailabilityCheckPacketFilter()));
        PacketHandlerFactory<DatagramPacket> packetHandlerFactory = new BaseUDPHandlerFactory<DatagramPacket>(dispatchHandler, filterChain, ignoreAddressFilter);
        InetSocketAddress bindAddress = new InetSocketAddress(configuration.getUdpBindIp(), configuration.getUdpBindPort());
        return new UDPReceiver(udpReceiverName, packetHandlerFactory, worker, configuration.getUdpReceiveBufferSize(), bindAddress);
    }

    public TCPReceiver createTcpReceiver(String name, DataReceiverGroupConfiguration configuration, DispatchHandler dispatchHandler, AddressFilter ignoreAddressFilter) {
        String tcpReceiverName = String.format("Pinpoint-TCP-%s-Receiver", name);
        InetSocketAddress bindAddress = new InetSocketAddress(configuration.getTcpBindIp(), configuration.getTcpBindPort());

        return new TCPReceiver(tcpReceiverName, dispatchHandler, worker, bindAddress, ignoreAddressFilter);
    }

    @PostConstruct
    @Override
    public void start() {
        logger.info("start() started");

        worker.setMetricRegistry(metricRegistry);
        worker.start();

        for (DataReceiver receiver : receiverList) {
            receiver.start();
        }

        logger.info("start() completed");
    }

    @Override
    public void shutdown() {
        logger.info("shutdown() started");

        for (DataReceiver receiver : receiverList) {
            receiver.shutdown();
        }

        worker.shutdown();

        logger.info("shutdown() completed");
    }

}
