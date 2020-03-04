/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;

import org.jboss.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class TestPinpointServerAcceptor {

    public static final String LOCALHOST = "localhost";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointServerAcceptor serverAcceptor;

    public TestPinpointServerAcceptor() {
        this((ServerMessageListenerFactory) null);
    }

    public TestPinpointServerAcceptor(ServerMessageListenerFactory messageListenerFactory) {
        this(messageListenerFactory, null);
    }

    public TestPinpointServerAcceptor(ServerMessageListenerFactory messageListenerFactory, ServerStreamChannelMessageHandler streamChannelMessageHandler) {
        this(messageListenerFactory, streamChannelMessageHandler, null);
    }

    public TestPinpointServerAcceptor(ChannelHandler messageHandler) {
        this(null, null, messageHandler);
    }

    public TestPinpointServerAcceptor(ServerMessageListenerFactory messageListenerFactory, ServerStreamChannelMessageHandler streamChannelMessageHandler, ChannelHandler messageHandler) {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();

        if (messageListenerFactory != null) {
            serverAcceptor.setMessageListenerFactory(messageListenerFactory);
        }

        if (streamChannelMessageHandler != null) {
            serverAcceptor.setServerStreamChannelMessageHandler(streamChannelMessageHandler);
        }

        if (messageHandler != null) {
            serverAcceptor.setMessageHandler(messageHandler);
        }

        this.serverAcceptor = serverAcceptor;
    }

    public int bind() {
        int port = SocketUtils.findAvailableTcpPort(47000);
        return bind(port);
    }

    public int bind(int port) {
        logger.info("bind port:{}", port);
        serverAcceptor.bind(LOCALHOST, port);
        return port;
    }

    public void assertAwaitClientConnected(int maxWaitTime) {
        boolean clientConnected = awaitClientConnected(maxWaitTime);
        org.junit.Assert.assertTrue(clientConnected);
    }

    public boolean awaitClientConnected(int maxWaitTime) {
        Assert.isTrue(maxWaitTime > 100, "maxWaitTime must be greater than 100");

        TestAwaitUtils awaitUtils = new TestAwaitUtils(100, maxWaitTime);
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !serverAcceptor.getWritableSocketList().isEmpty();
            }
        });

        return pass;
    }

    public void assertAwaitClientConnected(int expectedConnectedClientCount, int maxWaitTime) {
        boolean clientConnected = awaitClientConnected(expectedConnectedClientCount, maxWaitTime);
        org.junit.Assert.assertTrue(clientConnected);
    }

    public boolean awaitClientConnected(final int expectedConnectedClientCount, int maxWaitTime) {
        Assert.isTrue(maxWaitTime > 100, "maxWaitTime must be greater than 100");

        TestAwaitUtils awaitUtils = new TestAwaitUtils(100, maxWaitTime);
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return CollectionUtils.nullSafeSize(serverAcceptor.getWritableSocketList()) == expectedConnectedClientCount;
            }
        });

        return pass;
    }

    public int getConnectedClientCount() {
        return CollectionUtils.nullSafeSize(serverAcceptor.getWritableSocketList());
    }

    public List<PinpointSocket> getConnectedPinpointSocketList() {
        return serverAcceptor.getWritableSocketList();
    }

    public void close() {
        logger.info("close");
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }
    }

    public static void staticClose(TestPinpointServerAcceptor testPinpointServerAcceptor) {
        if (testPinpointServerAcceptor != null) {
            testPinpointServerAcceptor.close();
        }
    }

}
