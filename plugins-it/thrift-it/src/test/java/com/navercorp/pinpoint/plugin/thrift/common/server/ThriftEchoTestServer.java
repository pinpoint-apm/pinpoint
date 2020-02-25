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

package com.navercorp.pinpoint.plugin.thrift.common.server;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.AsyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.SyncEchoTestClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public abstract class ThriftEchoTestServer<T extends TServer> implements EchoTestServer {

    private final T server;

    protected final TestEnvironment environment;

    protected ThriftEchoTestServer(T server, TestEnvironment environment) {
        if (server == null) {
            throw new IllegalArgumentException("server cannot be null");
        }
        this.server = server;
        this.environment = environment;
    }

    @Override
    public void start(ExecutorService executor) {
        if (this.server.isServing()) {
            return;
        }

        CountDownLatch waitToServeLatch = new CountDownLatch(1);
        server.setServerEventHandler(new WaitToServeHandler(waitToServeLatch));

        executor.execute(new Runnable() {
            @Override
            public void run() {
                server.serve();
            }
        });

        boolean started = false;
        try {
            started = waitToServeLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!started) {
            throw new IllegalStateException("Failed to start EchoThriftServer.");
        }
    }

    @Override
    public void stop() {
        this.server.stop();
    }

    @Override
    public void verifyTraces(PluginTestVerifier verifier) throws Exception {
        this.verifyServerTraces(verifier);
    }

    protected abstract void verifyServerTraces(PluginTestVerifier verifier) throws Exception;

    public abstract SyncEchoTestClient getSynchronousClient() throws TTransportException;

    public abstract AsyncEchoTestClient getAsynchronousClient() throws IOException;


    private class WaitToServeHandler implements TServerEventHandler {

        private final CountDownLatch waitToServeLatch;

        public WaitToServeHandler(CountDownLatch waitToServeLatch) {
            this.waitToServeLatch = waitToServeLatch;
        }

        @Override
        public void preServe() {
            waitToServeLatch.countDown();
        }

        @Override
        public ServerContext createContext(TProtocol tProtocol, TProtocol tProtocol1) {
            return null;
        }

        @Override
        public void deleteContext(ServerContext serverContext, TProtocol tProtocol, TProtocol tProtocol1) {

        }

        @Override
        public void processContext(ServerContext serverContext, TTransport tTransport, TTransport tTransport1) {

        }
    }

}
