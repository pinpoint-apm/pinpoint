/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.thrift.it;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.it.plugin.thrift.common.client.EchoTestClient;
import com.navercorp.pinpoint.it.plugin.thrift.common.server.EchoTestServer;
import org.apache.thrift.transport.TTransportException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author HyunGil Jeong
 */
public abstract class EchoTestRunner<T extends EchoTestServer> {

    private static ExecutorService SERVER_EXECUTOR;

    private T echoServer;

    PluginTestVerifier verifier;

    @BeforeAll
    public static void setUpBeforeClass() {
        SERVER_EXECUTOR = Executors.newSingleThreadExecutor();
    }

    @BeforeEach
    public void setUp() throws TTransportException {
        this.echoServer = createEchoServer(new TestEnvironment());
        this.verifier = PluginTestVerifierHolder.getInstance();
        this.echoServer.start(SERVER_EXECUTOR);
    }

    @AfterEach
    public void tearDown() {
        if (this.echoServer != null) {
            this.echoServer.stop();
        }
    }

    @AfterAll
    public static void tearDownAfterClass() {
        SERVER_EXECUTOR.shutdown();
    }

    protected T getServer() {
        return echoServer;
    }

    protected String invokeAndVerify(EchoTestClient echoClient, String message) throws Exception {
        try {
            return echoClient.echo(message);
        } finally {
            echoClient.close();
            // give a chance to flush out span data
            Thread.sleep(500L);
            this.verifyTraces(echoClient, message);
        }
    }

    protected abstract T createEchoServer(TestEnvironment environment) throws TTransportException;

    private void verifyTraces(EchoTestClient echoClient, String expectedMessage) throws Exception {
        this.verifier.printCache(System.out);
        echoClient.verifyTraces(this.verifier, expectedMessage);
        this.echoServer.verifyTraces(this.verifier);
        this.verifier.verifyTraceCount(0);
    }
}
