/*
 * Copyright 2022 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.AsyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.EchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.SyncEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.client.SyncEchoTestClient014;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.AbstractNonblockingServer;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.root;

/**
 * @author HyunGil Jeong
 */
public abstract class AsyncEchoTestServer014<T extends AbstractNonblockingServer> extends ThriftEchoTestServer<T> {

    protected AsyncEchoTestServer014(T server, TestEnvironment environment) {
        super(server, environment);
    }

    @Override
    public void verifyServerTraces(PluginTestVerifier verifier) throws Exception {
        final InetSocketAddress socketAddress = super.environment.getServerAddress();
        final String address = SocketAddressUtils.getAddressFirst(socketAddress);
        verifier.verifyTraceCount(2);
        // Method process = TBaseAsyncProcessor.class.getDeclaredMethod("process", AsyncFrameBuffer.class);
        Method process = TBinaryProtocol.class.getDeclaredMethod("readMessageEnd");
        // RootSpan
        verifier.verifyTrace(root("THRIFT_SERVER", // ServiceType,
                "Thrift Server Invocation", // Method
                "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo", // rpc
                HostAndPort.toHostAndPortString(address, socketAddress.getPort()), // endPoint
                address // remoteAddress
        ));
        // SpanEvent - TBaseAsyncProcessor.process
        verifier.verifyTrace(event("THRIFT_SERVER_INTERNAL", process, annotation("thrift.url", "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo")));
    }

    public static class AsyncEchoTestServerFactory {

        private static TProcessor getAsyncProcessor() {
            return new EchoService.AsyncProcessor<EchoService.AsyncIface>(new EchoService.AsyncIface() {
                @Override
                public void echo(String message, AsyncMethodCallback<String> resultHandler) throws TException {
                    resultHandler.onComplete(message);
                }
            });
        }

        public static AsyncEchoTestServer014<TThreadedSelectorServer> threadedSelectorServer(
                final TestEnvironment environment) throws TTransportException {
            TThreadedSelectorServer server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(
                    new TNonblockingServerSocket(environment.getPort())).processor(getAsyncProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new AsyncEchoTestServer014<TThreadedSelectorServer>(server, environment) {
                @Override
                public EchoTestClient getSynchronousClient() throws Exception {
                    return new SyncEchoTestClient014.ClientForNonblockingServer(environment);
                }

                @Override
                public EchoTestClient getAsynchronousClient() throws Exception {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static AsyncEchoTestServer014<TNonblockingServer> nonblockingServer(final TestEnvironment environment)
                throws TTransportException {
            TNonblockingServer server = new TNonblockingServer(new TNonblockingServer.Args(
                    new TNonblockingServerSocket(environment.getPort())).processor(getAsyncProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new AsyncEchoTestServer014<TNonblockingServer>(server, environment) {
                @Override
                public EchoTestClient getSynchronousClient() throws Exception {
                    return new SyncEchoTestClient014.ClientForNonblockingServer(environment);
                }

                @Override
                public EchoTestClient getAsynchronousClient() throws Exception {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static AsyncEchoTestServer014<THsHaServer> halfSyncHalfAsyncServer(final TestEnvironment environment)
                throws TTransportException {
            THsHaServer server = new THsHaServer(new THsHaServer.Args(new TNonblockingServerSocket(
                    environment.getPort())).processor(getAsyncProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new AsyncEchoTestServer014<THsHaServer>(server, environment) {
                @Override
                public EchoTestClient getSynchronousClient() throws Exception {
                    return new SyncEchoTestClient014.ClientForNonblockingServer(environment);
                }

                @Override
                public EchoTestClient getAsynchronousClient() throws Exception {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }
    }

}
