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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.TestAwaitTaskUtils;
import com.navercorp.pinpoint.rpc.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientHandshaker;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HandshakeTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Timer timer = null;

    private static int bindPort;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(10, 500);


    @BeforeClass
    public static void setUp() throws IOException {
        timer = TimerFactory.createHashedWheelTimer(HandshakeTest.class.getSimpleName(), 100, TimeUnit.MILLISECONDS, 512);
        bindPort = SocketUtils.findAvailableTcpPort();
    }

    @AfterClass
    public static void tearDown() {
        if (timer != null) {
            timer.stop();
        }
    }

    // simple test
    @Test
    public void handshakeTest1() throws InterruptedException {
        final PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_INSTANCE);

        PinpointClientFactory clientFactory1 = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());
        PinpointClientFactory clientFactory2 = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), null);
        try {
            PinpointClient client = clientFactory1.connect("127.0.0.1", bindPort);
            PinpointClient client2 = clientFactory2.connect("127.0.0.1", bindPort);

            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    List<PinpointSocket> writableServerList = serverAcceptor.getWritableSocketList();
                    return writableServerList.size() == 2;
                }
            });

            List<PinpointSocket> writableServerList = serverAcceptor.getWritableSocketList();
            if (writableServerList.size() != 2) {
                Assert.fail();
            }

            PinpointRPCTestUtils.close(client, client2);
        } finally {
            clientFactory1.release();
            clientFactory2.release();

            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void handshakeTest2() throws InterruptedException {
        final PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_INSTANCE);

        Map<String, Object> params = PinpointRPCTestUtils.getParams();
        
        PinpointClientFactory clientFactory1 = PinpointRPCTestUtils.createClientFactory(PinpointRPCTestUtils.getParams(), PinpointRPCTestUtils.createEchoClientListener());

        try {
            PinpointClient client = clientFactory1.connect("127.0.0.1", bindPort);
            awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    List<PinpointSocket> writableServerList = serverAcceptor.getWritableSocketList();
                    return writableServerList.size() == 1;
                }
            });

            PinpointSocket writableServer = getWritableServer("application", "agent", (Long) params.get(HandshakePropertyType.START_TIMESTAMP.getName()), serverAcceptor.getWritableSocketList());
            Assert.assertNotNull(writableServer);

            writableServer = getWritableServer("application", "agent", (Long) params.get(HandshakePropertyType.START_TIMESTAMP.getName()) + 1, serverAcceptor.getWritableSocketList());
            Assert.assertNull(writableServer);

            PinpointRPCTestUtils.close(client);
        } finally {
            clientFactory1.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void testExecuteCompleteWithoutStart() {
        int retryInterval = 100;
        int maxHandshakeCount = 10;

        PinpointClientHandshaker handshaker = new PinpointClientHandshaker(timer, retryInterval, maxHandshakeCount);
        handshaker.handshakeComplete(null);

        Assert.assertEquals(null, handshaker.getHandshakeResult());

        Assert.assertTrue(handshaker.isFinished());
    }

    @Test
    public void testExecuteAbortWithoutStart() {
        int retryInterval = 100;
        int maxHandshakeCount = 10;

        PinpointClientHandshaker handshaker = new PinpointClientHandshaker(timer, retryInterval, maxHandshakeCount);
        handshaker.handshakeAbort();

        Assert.assertTrue(handshaker.isFinished());
    }

    private PinpointSocket getWritableServer(String applicationName, String agentId, long startTimeMillis, List<PinpointSocket> writableServerList) {
        if (applicationName == null) {
            return null;
        }

        if (agentId == null) {
            return null;
        }

        if (startTimeMillis <= 0) {
            return null;
        }

        List<PinpointSocket> result = new ArrayList<PinpointSocket>();

        for (PinpointSocket writableServer : writableServerList) {

            if (writableServer instanceof  PinpointServer) {
                Map agentProperties = ((PinpointServer)writableServer).getChannelProperties();

                if (!applicationName.equals(agentProperties.get(HandshakePropertyType.APPLICATION_NAME.getName()))) {
                    continue;
                }

                if (!agentId.equals(agentProperties.get(HandshakePropertyType.AGENT_ID.getName()))) {
                    continue;
                }

                if (startTimeMillis != (Long) agentProperties.get(HandshakePropertyType.START_TIMESTAMP.getName())) {
                    continue;
                }

                result.add(writableServer);
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        if (result.size() == 1) {
            return result.get(0);
        } else {
            logger.warn("Ambiguous Channel Context {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeMillis, result);
            return null;
        }
    }

}
