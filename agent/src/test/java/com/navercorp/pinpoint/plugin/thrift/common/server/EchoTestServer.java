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

package com.navercorp.pinpoint.plugin.thrift.common.server;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.AsyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.SyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @author HyunGil Jeong
 */
public abstract class EchoTestServer<T extends TServer> {

    protected static class EchoServiceHandler implements EchoService.Iface {
        @Override
        public String echo(String message) throws TException {
            return message;
        }
    }

    protected static class EchoServiceAsyncHandler implements EchoService.AsyncIface {

        private final EchoServiceHandler syncHandler = new EchoServiceHandler();

        @SuppressWarnings("unchecked")
        @Override
        public void echo(String message, AsyncMethodCallback resultHandler) throws TException {
            try {
                final String echo = this.syncHandler.echo(message);
                resultHandler.onComplete(echo);
            } catch (Exception e) {
                resultHandler.onError(e);
            }
        }
    }

    private final T server;

    protected final TestEnvironment environment;

    protected EchoTestServer(T server, TestEnvironment environment) throws TTransportException {
        if (server == null) {
            throw new IllegalArgumentException("server cannot be null");
        }
        this.server = server;
        this.environment = environment;
    }

    public void start(ExecutorService executor) throws TTransportException, InterruptedException {
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

        waitToServeLatch.await();
    }

    public void stop() {
        this.server.stop();
    }

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
