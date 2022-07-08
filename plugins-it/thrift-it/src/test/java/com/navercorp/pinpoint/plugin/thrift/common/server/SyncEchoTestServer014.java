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
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.root;

/**
 * @author HyunGil Jeong
 */
public abstract class SyncEchoTestServer014<T extends TServer> extends ThriftEchoTestServer<T> {

    protected SyncEchoTestServer014(T server, TestEnvironment environment) {
        super(server, environment);
    }

    @Override
    public void verifyServerTraces(PluginTestVerifier verifier) throws Exception {
        final InetSocketAddress socketAddress = super.environment.getServerAddress();
        final String address = SocketAddressUtils.getAddressFirst(socketAddress);
        verifier.verifyTraceCount(2);
        Method process = TBinaryProtocol.class.getDeclaredMethod("readMessageEnd");
        verifier.verifyTrace(
                // RootSpan - Thrift Server Invocation
                // refer to TBaseProcessorProcessInterceptor.finalizeSpan(...)
                root("THRIFT_SERVER", // ServiceType,
                        "Thrift Server Invocation", // Method
                        "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo", // rpc
                        HostAndPort.toHostAndPortString(address, socketAddress.getPort()), // endPoint
                        address), // remoteAddress
                // SpanEvent - TBaseProcessor.process
                event("THRIFT_SERVER_INTERNAL", process, annotation("thrift.url", "com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo")));
    }

    public static class SyncEchoTestServerFactory {

        private static TProcessor getProcessor() {
            return new EchoService.Processor<EchoService.Iface>(new EchoService.Iface() {
                @Override
                public String echo(String message) throws TException {
                    return message;
                }
            });
        }

        public static SyncEchoTestServer014<TSimpleServer> simpleServer(final TestEnvironment environment)
                throws TTransportException {
            TSimpleServer server = new TSimpleServer(new TSimpleServer.Args(new TServerSocket(environment.getPort()))
                    .processor(getProcessor()).inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer014<TSimpleServer>(server, environment) {
                @Override
                public EchoTestClient getSynchronousClient() throws Exception {
                    return new SyncEchoTestClient014.Client(environment);
                }

                @Override
                public EchoTestClient getAsynchronousClient() throws Exception {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static SyncEchoTestServer014<TThreadPoolServer> threadedPoolServer(final TestEnvironment environment)
                throws TTransportException {
            TThreadPoolServer server = new TThreadPoolServer(new TThreadPoolServer.Args(new TServerSocket(
                    environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer014<TThreadPoolServer>(server, environment) {
                @Override
                public EchoTestClient getSynchronousClient() throws Exception {
                    return new SyncEchoTestClient014.Client(environment);
                }

                @Override
                public EchoTestClient getAsynchronousClient() throws Exception {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static SyncEchoTestServer014<TThreadedSelectorServer> threadedSelectorServer(
                final TestEnvironment environment) throws TTransportException {
            TThreadedSelectorServer server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(
                    new TNonblockingServerSocket(environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer014<TThreadedSelectorServer>(server, environment) {
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

        public static SyncEchoTestServer014<TNonblockingServer> nonblockingServer(final TestEnvironment environment)
                throws TTransportException {
            TNonblockingServer server = new TNonblockingServer(new TNonblockingServer.Args(
                    new TNonblockingServerSocket(environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer014<TNonblockingServer>(server, environment) {
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

        public static SyncEchoTestServer014<THsHaServer> halfSyncHalfAsyncServer(final TestEnvironment environment)
                throws TTransportException {
            THsHaServer server = new THsHaServer(new THsHaServer.Args(new TNonblockingServerSocket(
                    environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer014<THsHaServer>(server, environment) {
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
