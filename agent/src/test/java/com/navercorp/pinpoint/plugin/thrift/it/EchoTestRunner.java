/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.it;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.EchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.server.EchoTestServer;

/**
 * @author HyunGil Jeong
 */
public abstract class EchoTestRunner<T extends TServer> {

    private static ExecutorService SERVER_EXECUTOR;

    private EchoTestServer<T> echoServer;

    PluginTestVerifier verifier;

    @BeforeClass
    public static void setUpBeforeClass() throws InterruptedException {
        SERVER_EXECUTOR = Executors.newSingleThreadExecutor();
    }

    @Before
    public void setUp() throws TTransportException, InterruptedException {
        this.echoServer = createEchoServer(new TestEnvironment());
        this.verifier = PluginTestVerifierHolder.getInstance();
        this.echoServer.start(SERVER_EXECUTOR);
    }

    @After
    public void tearDown() {
        if (this.echoServer != null) {
            this.echoServer.stop();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
        SERVER_EXECUTOR.shutdown();
    }

    protected String invokeEcho(String message) throws Exception {
        final EchoTestClient echoClient = this.echoServer.getSynchronousClient();
        return invokeAndVerify(echoClient, message);
    }

    protected String invokeEchoAsync(String message) throws Exception {
        final EchoTestClient echoClient = this.echoServer.getAsynchronousClient();
        return invokeAndVerify(echoClient, message);
    }

    private String invokeAndVerify(EchoTestClient echoClient, String message) throws Exception {
        try {
            return echoClient.echo(message);
        } finally {
            echoClient.close();
            // give a chance to flush out span data
            Thread.sleep(500L);
            this.verifyTraces(echoClient, message);
        }
    }

    protected abstract EchoTestServer<T> createEchoServer(TestEnvironment environment) throws TTransportException;

    private void verifyTraces(EchoTestClient echoClient, String expectedMessage) throws Exception {
        this.verifier.printCache(System.out);
        echoClient.verifyTraces(this.verifier, expectedMessage);
        this.echoServer.verifyTraces(this.verifier);
    }

}
