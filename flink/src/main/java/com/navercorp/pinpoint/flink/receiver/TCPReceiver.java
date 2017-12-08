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

package com.navercorp.pinpoint.flink.receiver;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchWorker;
import com.navercorp.pinpoint.collector.receiver.tcp.SendPacketHandler;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class TCPReceiver {

    private final Logger logger = LoggerFactory.getLogger(TCPReceiver.class);

    private final String bindIp;
    private final int bindPort;

    private PinpointServerAcceptor serverAcceptor;

    private final DispatchWorker worker;
    private final SendPacketHandler sendPacketHandler;

    private final AddressFilter ignoreAddressFilter;

    public TCPReceiver(DispatchHandler dispatchHandler, DispatchWorker worker, String bindIp, int bindPort, List<String> ignoreAddressList) {
        this.bindIp = Objects.requireNonNull(bindIp, "bindIp must not be null");
        Assert.isTrue(bindPort > 0, "bindPort must be greater than 0");
        this.bindPort = bindPort;

        this.worker = Objects.requireNonNull(worker, "worker must not be null");

        Objects.requireNonNull(dispatchHandler, " must not be null");
        this.sendPacketHandler = new SendPacketHandler(dispatchHandler, new ThreadLocalHeaderTBaseDeserializerFactory<>(new FlinkHeaderTBaseDeserializerFactory()));

        this.ignoreAddressFilter = new IgnoreAddressFilter(ignoreAddressList);
    }

    @PostConstruct
    public void start() {
        ChannelFilter connectedFilter = new AddressFilterAdaptor(ignoreAddressFilter);
        PinpointServerAcceptor acceptor = new PinpointServerAcceptor(connectedFilter);
        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        acceptor.setMessageListener(new ServerMessageListener() {

            @Override
            public HandshakeResponseCode handleHandshake(Map properties) {
                return HandshakeResponseType.Success.SIMPLEX_COMMUNICATION;
            }

            @Override
            public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
                receive(sendPacket, pinpointSocket);
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
                requestResponse(requestPacket, pinpointSocket);
            }

            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            }
        });
        acceptor.bind(bindIp, bindPort);
        this.serverAcceptor = acceptor;
    }

    private void receive(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                sendPacketHandler.handle(sendPacket, pinpointSocket);
            }
        });
    }

    private void requestResponse(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        logger.warn("Not support requestResponse");
    }

    @PreDestroy
    public void stop() {
        logger.info("Pinpoint-TCP-Server stop");
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }
    }

}
