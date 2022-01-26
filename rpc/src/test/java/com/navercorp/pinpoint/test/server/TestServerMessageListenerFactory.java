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

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;

/**
 * @author Taejin Koo
 */
public class TestServerMessageListenerFactory implements ServerMessageListenerFactory {

    public enum HandshakeType {
        SIMPLEX,
        DUPLEX
    }

    public enum ResponseType {
        NO_RESPONSE,
        ECHO
    }

    private final HandshakeType handshakeType;
    private final ResponseType responseType;

    private final boolean singleton;
    private final AtomicReference<TestServerMessageListener> singletonReference = new AtomicReference<>();

    public TestServerMessageListenerFactory() {
        this(HandshakeType.SIMPLEX, ResponseType.ECHO);
    }

    public TestServerMessageListenerFactory(HandshakeType handshakeType) {
        this(handshakeType, ResponseType.ECHO, false);
    }

    public TestServerMessageListenerFactory(HandshakeType handshakeType, boolean singleton) {
        this(handshakeType, ResponseType.ECHO, singleton);
    }

    public TestServerMessageListenerFactory(HandshakeType handshakeType, ResponseType responseType) {
        this(handshakeType, responseType, false);
    }

    public TestServerMessageListenerFactory(HandshakeType handshakeType, ResponseType responseType, boolean singleton) {
        this.handshakeType = Objects.requireNonNull(handshakeType, "handshakeType");
        this.responseType = Objects.requireNonNull(responseType, "responseType");
        this.singleton = singleton;
    }

    @Override
    public TestServerMessageListener create() {
        if (singleton) {
            singletonReference.compareAndSet(null, new TestServerMessageListener(handshakeType, responseType));
            return singletonReference.get();
        }

        return new TestServerMessageListener(handshakeType, responseType);
    }

    public static TestServerMessageListener create(HandshakeType handshakeType, ResponseType responseType) {
        return new TestServerMessageListener(handshakeType, responseType);
    }

    public static class TestServerMessageListener implements ServerMessageListener {

        private final Logger logger = LogManager.getLogger(this.getClass());

        private final AtomicInteger handleSendCount = new AtomicInteger(0);
        private final AtomicInteger handleRequestCount = new AtomicInteger(0);
        private final AtomicInteger handlePingCount = new AtomicInteger(0);

        private final HandshakeType handshakeType;
        private final ResponseType responseType;

        public TestServerMessageListener(HandshakeType handshakeType, ResponseType responseType) {
            this.handshakeType = Objects.requireNonNull(handshakeType, "handshakeType");
            this.responseType = Objects.requireNonNull(responseType, "responseType");
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            handleSendCount.incrementAndGet();
            logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            handleRequestCount.incrementAndGet();

            logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

            if (responseType == ResponseType.ECHO) {
                pinpointSocket.response(requestPacket.getRequestId(), requestPacket.getPayload());
            }
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map<?, ?> properties) {
            logger.info("handleHandshake properties:{}", properties);

            if (handshakeType == HandshakeType.DUPLEX) {
                return HandshakeResponseCode.DUPLEX_COMMUNICATION;
            } else {
                return HandshakeResponseCode.SIMPLEX_COMMUNICATION;
            }
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            handlePingCount.incrementAndGet();

            logger.info("handlePing packet:{}, remote:{}", pingPacket, pinpointServer.getRemoteAddress());
        }

        public void awaitAssertExpectedSendCount(final int expectedCount, long maxWaitTime) {
            awaitAssertExpectedCount(expectedCount, handleSendCount, maxWaitTime);
        }

        public void awaitAssertExpectedRequestCount(final int expectedCount, long maxWaitTime) {
            awaitAssertExpectedCount(expectedCount, handleRequestCount, maxWaitTime);
        }

        public void awaitAssertExpectedPingCount(final int expectedCount, long maxWaitTime) {
            awaitAssertExpectedCount(expectedCount, handlePingCount, maxWaitTime);
        }

        private void awaitAssertExpectedCount(int expectedCount, AtomicInteger handlePingCount, long maxWaitTime) {
            if (maxWaitTime > 100) {
                Awaitility.waitAtMost(maxWaitTime, TimeUnit.MILLISECONDS)
                        .untilAtomic(handlePingCount, is(expectedCount));
            } else {
                Assert.assertTrue(expectedCount == handlePingCount.get());
            }
        }

        public boolean hasReceivedPing() {
            return handlePingCount.get() > 0;
        }

    }

}
