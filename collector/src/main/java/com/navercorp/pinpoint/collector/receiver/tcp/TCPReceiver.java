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

package com.navercorp.pinpoint.collector.receiver.tcp;

import com.navercorp.pinpoint.collector.receiver.DataReceiver;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchWorker;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class TCPReceiver implements DataReceiver {

    private final Logger logger;

    private final String name;

    private final InetSocketAddress bindAddress;
    private final List<InetAddress> ignoreAddressList;

    private PinpointServerAcceptor serverAcceptor;

    private final DispatchWorker worker;

    private final SendPacketHandler sendPacketHandler;
    private final RequestPacketHandler requestPacketHandler;


    public TCPReceiver(String name, DispatchHandler dispatchHandler, DispatchWorker worker, InetSocketAddress bindAddress, List<InetAddress> ignoreAddressList) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        logger = LoggerFactory.getLogger(name);

        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress must not be null");
        this.ignoreAddressList = ignoreAddressList;

        this.worker = Objects.requireNonNull(worker, "worker must not be null");

        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.sendPacketHandler = new SendPacketHandler(dispatchHandler);
        this.requestPacketHandler = new RequestPacketHandler(dispatchHandler);
    }

    @Override
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("{} start() started", name);
        }

        PinpointServerAcceptor acceptor = new PinpointServerAcceptor();
        if (!CollectionUtils.isEmpty(ignoreAddressList)) {
            InetAddress[] ignoreAddressArray = new InetAddress[ignoreAddressList.size()];
            acceptor.setIgnoreAddressList(ignoreAddressList.toArray(ignoreAddressArray));
        }

        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        acceptor.setMessageListener(new ServerMessageListener() {

            @Override
            public HandshakeResponseCode handleHandshake(Map properties) {
                return HandshakeResponseCode.SIMPLEX_COMMUNICATION;
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
        acceptor.bind(bindAddress);

        this.serverAcceptor = acceptor;

        if (logger.isInfoEnabled()) {
            logger.info("{} start() completed", name);
        }
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
        worker.execute(new Runnable() {
            @Override
            public void run() {
                requestPacketHandler.handle(requestPacket, pinpointSocket);
            }
        });
    }

    @Override
    public void shutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("{} shutdown() started", name);
        }

        if (serverAcceptor != null) {
            serverAcceptor.close();
        }

        if (logger.isInfoEnabled()) {
            logger.info("{} shutdown() completed", name);
        }
    }

}
