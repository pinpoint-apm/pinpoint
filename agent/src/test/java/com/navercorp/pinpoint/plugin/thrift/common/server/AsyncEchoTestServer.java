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

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.thrift.TBaseAsyncProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.AbstractNonblockingServer;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.plugin.thrift.common.client.AsyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.SyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;

/**
 * @author HyunGil Jeong
 */
public abstract class AsyncEchoTestServer<T extends AbstractNonblockingServer> extends EchoTestServer<T> {

    protected AsyncEchoTestServer(T server) throws TTransportException {
        super(server);
    }
    
    @Override
    public void verifyServerTraces(PluginTestVerifier verifier) throws Exception {
        verifier.verifyTraceCount(2);
        Method process = TBaseAsyncProcessor.class.getDeclaredMethod("process", AsyncFrameBuffer.class);
        // RootSpan
        verifier.verifyTrace(root(
                "THRIFT_SERVER", // ServiceType,
                "Thrift Server Invocation", // Method
                "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo", // rpc
                SERVER_ADDRESS.getHostName() + ":" + SERVER_ADDRESS.getPort(), // endPoint
                SERVER_ADDRESS.getHostName() // remoteAddress
        ));
        // SpanEvent - TBaseAsyncProcessor.process
        verifier.verifyTrace(event("THRIFT_SERVER_INTERNAL", process));
        verifier.verifyTraceCount(0);
    }

    public static class AsyncEchoTestServerFactory {
        
        private static TProcessor getAsyncProcessor() {
            return new EchoService.AsyncProcessor<EchoService.AsyncIface>(new EchoServiceAsyncHandler());
        }
        
        public static AsyncEchoTestServer<TThreadedSelectorServer> threadedSelectorServer() throws TTransportException {
            TThreadedSelectorServer server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(new TNonblockingServerSocket(SERVER_PORT))
                    .processor(getAsyncProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new AsyncEchoTestServer<TThreadedSelectorServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
        
        public static AsyncEchoTestServer<TNonblockingServer> nonblockingServer() throws TTransportException {
            TNonblockingServer server = new TNonblockingServer(new TNonblockingServer.Args(new TNonblockingServerSocket(SERVER_PORT))
                    .processor(getAsyncProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new AsyncEchoTestServer<TNonblockingServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
        
        public static AsyncEchoTestServer<THsHaServer> halfSyncHalfAsyncServer() throws TTransportException {
            THsHaServer server = new THsHaServer(new THsHaServer.Args(new TNonblockingServerSocket(SERVER_PORT))
                    .processor(getAsyncProcessor())
                    .inputProtocolFactory(PROTOCOL_FACTORY)
                    .outputProtocolFactory(PROTOCOL_FACTORY));
            return new AsyncEchoTestServer<THsHaServer>(server) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer();
                }
                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client();
                }
            };
        }
    }

}
