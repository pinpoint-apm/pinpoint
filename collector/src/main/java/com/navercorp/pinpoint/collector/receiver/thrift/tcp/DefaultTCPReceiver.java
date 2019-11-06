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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import com.navercorp.pinpoint.collector.receiver.thrift.PinpointServerAcceptorProvider;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
public class DefaultTCPReceiver implements TCPReceiver {

    private final Logger logger;

    private final String name;

    private final InetSocketAddress bindAddress;
    private final PinpointServerAcceptorProvider acceptorProvider;

    private PinpointServerAcceptor serverAcceptor;

    private final Executor executor;

    private final TCPPacketHandler tcpPacketHandler;


    public DefaultTCPReceiver(String name, TCPPacketHandler tcpPacketHandler, Executor executor, InetSocketAddress bindAddress, PinpointServerAcceptorProvider acceptorProvider) {
        this.name = Objects.requireNonNull(name, "name");
        this.logger = LoggerFactory.getLogger(name);

        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");

        this.acceptorProvider = Objects.requireNonNull(acceptorProvider, "acceptorProvider");
        this.executor = Objects.requireNonNull(executor, "executor");

        this.tcpPacketHandler = Objects.requireNonNull(tcpPacketHandler, "tcpPacketHandler");

    }

    @Override
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("{} start() started", name);
        }
        final PinpointServerAcceptor acceptor = newAcceptor();
        acceptor.bind(bindAddress);
        this.serverAcceptor = acceptor;
        if (logger.isInfoEnabled()) {
            logger.info("{} start() completed", name);
        }
    }

    private PinpointServerAcceptor newAcceptor() {
        PinpointServerAcceptor acceptor = acceptorProvider.get();

        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        acceptor.setMessageListenerFactory(new TCPReceiverServerMessageListenerFactory(executor, tcpPacketHandler));
        return acceptor;
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
