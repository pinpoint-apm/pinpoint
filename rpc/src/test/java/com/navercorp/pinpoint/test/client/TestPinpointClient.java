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

package com.navercorp.pinpoint.test.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.LoggingStateChangeEventListener;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;

import java.util.Collections;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class TestPinpointClient {

    private final PinpointClientFactory pinpointClientFactory;
    private PinpointClient pinpointClient;

    public TestPinpointClient() {
        this(Collections.<String, Object>emptyMap());
    }

    public TestPinpointClient(Map<String, Object> param) {
        this(null, param);
    }

    public TestPinpointClient(MessageListener messageListener) {
        this(messageListener, (ServerStreamChannelMessageHandler) null);
    }

    public TestPinpointClient(MessageListener messageListener, ServerStreamChannelMessageHandler serverStreamChannelMessageHandler) {
        this(messageListener, serverStreamChannelMessageHandler, Collections.<String, Object>emptyMap());
    }

    public TestPinpointClient(MessageListener messageListener, Map<String, Object> param) {
        this(messageListener, null, param);
    }

    public TestPinpointClient(MessageListener messageListener, ServerStreamChannelMessageHandler serverStreamChannelMessageHandler, Map<String, Object> param) {
        Assert.requireNonNull(param, "param");

        PinpointClientFactory pinpointClientFactory = new DefaultPinpointClientFactory();
        pinpointClientFactory.setProperties(param);
        pinpointClientFactory.addStateChangeEventListener(LoggingStateChangeEventListener.INSTANCE);

        if (messageListener != null) {
            pinpointClientFactory.setMessageListener(messageListener);
        }

        if (serverStreamChannelMessageHandler != null) {
            pinpointClientFactory.setServerStreamChannelMessageHandler(serverStreamChannelMessageHandler);
        }

        this.pinpointClientFactory = pinpointClientFactory;
    }

    public TestPinpointClient(PinpointClientFactory pinpointClientFactory) {
        this.pinpointClientFactory = Assert.requireNonNull(pinpointClientFactory, "pinpointClientFactory");
    }

    public void connect(int port) {
        connect(TestPinpointServerAcceptor.LOCALHOST, port);
    }

    public void connect(String host, int port) {
        this.pinpointClient = pinpointClientFactory.connect(host, port);
    }

    public ClientStreamChannel openStream(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) throws StreamException {
        Assert.requireNonNull(pinpointClient, "pinpointClient");
        return pinpointClient.openStream(payload, streamChannelEventHandler);
    }

    public void disconnect() {
        if (pinpointClient != null) {
            pinpointClient.close();
        }
    }

    public void releaseConnectionFactory() {
        if (pinpointClientFactory != null) {
            pinpointClientFactory.release();
        }
    }

    public void closeAll() {
        if (pinpointClient != null) {
            pinpointClient.close();
        }

        if (pinpointClientFactory != null) {
            pinpointClientFactory.release();
        }
    }

    public static void staticCloseAll(TestPinpointClient pinpointClient) {
        if (pinpointClient != null) {
            pinpointClient.closeAll();
        }
    }

}
