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

import com.navercorp.pinpoint.profiler.TestAwaitTaskUtils;
import com.navercorp.pinpoint.profiler.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.server.LoggingServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.util.Collections;

/**
 * @author emeroad
 */
public class TcpDataSenderReconnectTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = SocketUtils.findAvailableTcpPort(50050);
    public static final String HOST = "127.0.0.1";

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 5000);

    public PinpointServerAcceptor serverAcceptorStart() {
        PinpointServerAcceptor serverAcceptor = new PinpointServerAcceptor();
        serverAcceptor.setMessageListenerFactory(new LoggingServerMessageListenerFactory(true));
        serverAcceptor.bind(HOST, PORT);
        return serverAcceptor;
    }

    @Test
    public void connectAndSend() throws InterruptedException {
        PinpointServerAcceptor oldAcceptor = serverAcceptorStart();

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        TcpDataSender sender = new TcpDataSender(this.getClass().getName(), HOST, PORT, clientFactory);
        waitClientConnected(oldAcceptor);

        oldAcceptor.close();
        waitClientDisconnected(sender);

        logger.debug("Server start------------------");
        PinpointServerAcceptor serverAcceptor = serverAcceptorStart();
        waitClientConnected(serverAcceptor);

        logger.debug("sendMessage------------------");
        sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

        Thread.sleep(500);
        logger.debug("sender stop------------------");
        sender.stop();

        serverAcceptor.close();
        clientFactory.release();
    }
    
    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setWriteTimeoutMillis(1000 * 3);
        clientFactory.setRequestTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.EMPTY_MAP);

        return clientFactory;
    }

    private void waitClientDisconnected(final TcpDataSender sender) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !sender.isConnected();
            }
        });

        Assert.assertTrue(pass);
    }

    private void waitClientConnected(final PinpointServerAcceptor acceptor) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !acceptor.getWritableSocketList().isEmpty();
            }
        });

        Assert.assertTrue(pass);
    }

}
