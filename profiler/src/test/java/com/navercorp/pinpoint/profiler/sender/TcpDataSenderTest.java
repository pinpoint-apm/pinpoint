/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.server.CountCheckServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TcpDataSenderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = SocketUtils.findAvailableTcpPort(50050);
    public static final String HOST = "127.0.0.1";

    private PinpointServerAcceptor serverAcceptor;

    public void serverStart(CountDownLatch sendLatch) {
        serverAcceptor = new PinpointServerAcceptor();

        CountCheckServerMessageListenerFactory countCheckServerMessageListenerFactory = new CountCheckServerMessageListenerFactory();
        countCheckServerMessageListenerFactory.setSendCountDownLatch(sendLatch);

        serverAcceptor.setMessageListenerFactory(countCheckServerMessageListenerFactory);
        serverAcceptor.bind(HOST, PORT);
    }

    @After
    public void serverShutdown() {
        if (serverAcceptor != null) {
            serverAcceptor.close();
        }
    }

    @Test
    public void connectAndSend() throws InterruptedException {
        CountDownLatch sendLatch = new CountDownLatch(2);

        serverStart(sendLatch);

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        TcpDataSender sender = new TcpDataSender(this.getClass().getName(), HOST, PORT, clientFactory);
        try {
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));


            boolean received = sendLatch.await(1000, TimeUnit.MILLISECONDS);
            Assert.assertTrue(received);
        } finally {
            sender.stop();
            
            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }
    
    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setWriteTimeoutMillis(1000 * 3);
        clientFactory.setRequestTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.<String, Object>emptyMap());

        return clientFactory;
    }

}
