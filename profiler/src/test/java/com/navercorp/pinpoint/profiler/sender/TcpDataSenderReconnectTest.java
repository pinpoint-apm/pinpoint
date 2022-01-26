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
import com.navercorp.pinpoint.rpc.server.LoggingServerMessageListenerFactory;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TcpDataSenderReconnectTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

        private ConditionFactory awaitility() {
        ConditionFactory conditionFactory = Awaitility.await()
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .timeout(5000, TimeUnit.MILLISECONDS);
        return conditionFactory;
    }

    @Test
    public void connectAndSend() throws InterruptedException {
        TestPinpointServerAcceptor oldTestPinpointServerAcceptor = new TestPinpointServerAcceptor(new LoggingServerMessageListenerFactory(true));
        int bindPort = oldTestPinpointServerAcceptor.bind();

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        TcpDataSender sender = new TcpDataSender(this.getClass().getName(), TestPinpointServerAcceptor.LOCALHOST, bindPort, clientFactory);
        oldTestPinpointServerAcceptor.assertAwaitClientConnected(5000);

        oldTestPinpointServerAcceptor.close();
        waitClientDisconnected(sender);

        logger.debug("Server start------------------");
        TestPinpointServerAcceptor newTestPinpointServerAcceptor = new TestPinpointServerAcceptor(new LoggingServerMessageListenerFactory(true));
        newTestPinpointServerAcceptor.bind(bindPort);
        newTestPinpointServerAcceptor.assertAwaitClientConnected(5000);

        logger.debug("sendMessage------------------");
        sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

        Thread.sleep(500);
        logger.debug("sender stop------------------");
        sender.stop();

        newTestPinpointServerAcceptor.close();
        clientFactory.release();
    }
    
    private PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setWriteTimeoutMillis(1000 * 3);
        clientFactory.setRequestTimeoutMillis(1000 * 5);
        clientFactory.setProperties(Collections.<String, Object>emptyMap());

        return clientFactory;
    }

    private void waitClientDisconnected(final TcpDataSender sender) {
            awaitility().until(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return sender.isConnected();
                }
            }, Matchers.not(true));
    }

}
