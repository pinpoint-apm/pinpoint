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
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import org.junit.Test;

import java.util.Collections;

/**
 * @author emeroad
 */
public class TcpDataSenderTest {

    @Test
    public void connectAndSend() throws InterruptedException {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClientFactory clientFactory = createPinpointClientFactory();

        TcpDataSender sender = new TcpDataSender(this.getClass().getName(), TestPinpointServerAcceptor.LOCALHOST, bindPort, clientFactory);
        try {
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

            serverMessageListener.awaitAssertExpectedSendCount(2, 1000);
        } finally {
            sender.stop();
            
            if (clientFactory != null) {
                clientFactory.release();
            }

            testPinpointServerAcceptor.close();
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
