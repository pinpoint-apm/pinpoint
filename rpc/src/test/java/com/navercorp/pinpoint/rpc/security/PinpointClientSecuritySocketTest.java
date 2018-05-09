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

package com.navercorp.pinpoint.rpc.security;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.ClientCodecPipelineFactory;
import com.navercorp.pinpoint.rpc.client.DefaultConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerCodecPipelineFactory;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.util.HashedWheelTimer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class PinpointClientSecuritySocketTest {

    private static final String LOCAL_HOST = "127.0.0.1";

    private static String AUTH_KEY = "secret";

    private static String MESSAGE = "hello";

    private HashedWheelTimer timer;
    private AuthenticationAwaitTaskManager awaitTaskManager;

    @Before
    public void setUp() {
        timer = TimerFactory.createHashedWheelTimer(PinpointClientSecuritySocketTest.class.getName() + "-TIMER", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();

        awaitTaskManager = new AuthenticationAwaitTaskManager(timer, 300);
    }

    @After
    public void tearDown() {
        if (timer != null) {
            timer.stop();
        }
    }

    // 1. success authentication,
    // 2. success send & receive
    @Test
    public void authenticationSuccessTest() throws Exception {
        int bindPort = SocketUtils.findAvailableTcpPort();

        PinpointServerAcceptor server = null;

        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;
        try {
            server = createServer(bindPort);
            clientFactory = createClientFactory(AUTH_KEY);
            client = clientFactory.connect(LOCAL_HOST, bindPort);

            Future<ResponseMessage> hello = client.request(MESSAGE.getBytes());
            hello.await();
            ResponseMessage result = hello.getResult();

            Assert.assertEquals(MESSAGE, new String(result.getMessage()));
        } finally {
            PinpointRPCTestUtils.close(server);
            PinpointRPCTestUtils.close(client);

            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }

    // 1. fail authentication,
    // 2. channel close
    // 3. get throw exception
    @Test(expected = PinpointSocketException.class)
    public void authenticationFailTest() throws Exception {
        int bindPort = SocketUtils.findAvailableTcpPort();

        PinpointServerAcceptor server = null;

        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;
        try {
            server = createServer(bindPort);
            clientFactory = createClientFactory(AUTH_KEY + "fail");
            client = clientFactory.connect(LOCAL_HOST, bindPort);
        } finally {
            PinpointRPCTestUtils.close(server);
            PinpointRPCTestUtils.close(client);

            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }

    // 1. fail authentication,
    // 2. channel close
    // 3. get throw exception
    @Test(expected = PinpointSocketException.class)
    public void authenticationTimeExpiredTest() throws Exception {
        int bindPort = SocketUtils.findAvailableTcpPort();

        PinpointServerAcceptor server = null;

        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;
        try {
            server = createServer(bindPort, 1000);
            clientFactory = createClientFactory(AUTH_KEY);
            client = clientFactory.connect(LOCAL_HOST, bindPort);
        } finally {
            PinpointRPCTestUtils.close(server);
            PinpointRPCTestUtils.close(client);

            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }

    private PinpointServerAcceptor createServer(int bindPort) {
        return createServer(bindPort, 1);
    }

    private PinpointServerAcceptor createServer(int bindPort, long waitingTimeMillis) {
        AuthenticationManager authenticationManager = MockAuthenticationManager.createServer(AUTH_KEY, waitingTimeMillis);
        AuthenticationServerHandler authenticationServerHandler = new AuthenticationServerHandler(authenticationManager);

        PinpointServerAcceptor server = new PinpointServerAcceptor(ChannelFilter.BYPASS, new TestServerPipelineFactory(authenticationServerHandler));
        server.bind(LOCAL_HOST, bindPort);

        server.setMessageListener(new PinpointRPCTestUtils.EchoServerListener());
        return server;
    }

    private PinpointClientFactory createClientFactory(String authKey) {
        DefaultConnectionFactoryProvider connectionFactoryProvider = new DefaultConnectionFactoryProvider(new TestClientPipelineFactory(awaitTaskManager, authKey));
        return new DefaultPinpointClientFactory(1, 1, connectionFactoryProvider);
    }

    private static class TestServerPipelineFactory implements PipelineFactory {

        private final AuthenticationServerHandler authenticationHandler;

        private TestServerPipelineFactory(AuthenticationServerHandler authenticationHandler) {
            this.authenticationHandler = authenticationHandler;
        }

        private final ServerCodecPipelineFactory pipelineFactory = new ServerCodecPipelineFactory();

        @Override
        public ChannelPipeline newPipeline() {
            ChannelPipeline pipeline = pipelineFactory.newPipeline();
            pipeline.addLast("auth", authenticationHandler);
            return pipeline;
        }
    }


    private static class TestClientPipelineFactory implements PipelineFactory {

        private final AuthenticationAwaitTaskManager awaitTaskManager;
        private final String authKey;

        public TestClientPipelineFactory(AuthenticationAwaitTaskManager awaitTaskManager, String authKey) {
            this.awaitTaskManager = awaitTaskManager;
            this.authKey = authKey;
        }

        @Override
        public ChannelPipeline newPipeline() {
            ClientCodecPipelineFactory clientCodecPipelineFactory = new ClientCodecPipelineFactory();
            ChannelPipeline pipeline = clientCodecPipelineFactory.newPipeline();

            AuthenticationManager authenticationManager = MockAuthenticationManager.createClient();
            AuthenticationClientHandler authenticationHandler = new AuthenticationClientHandler(authenticationManager, authKey.getBytes(), awaitTaskManager);

            pipeline.addLast("auth", authenticationHandler);

            return pipeline;
        }

    }

}
