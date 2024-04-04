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

package com.pinpoint.test.plugin.server;

import com.pinpoint.test.plugin.TestEnvironment;
import com.pinpoint.test.plugin.client.AsyncEchoTestClient;
import com.pinpoint.test.plugin.client.SyncEchoTestClient;
import com.pinpoint.test.plugin.dto.EchoService;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public abstract class SyncEchoTestServer<T extends TServer> extends ThriftEchoTestServer<T> {

    protected SyncEchoTestServer(T server, TestEnvironment environment) {
        super(server, environment);
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

        public static SyncEchoTestServer<TSimpleServer> simpleServer(final TestEnvironment environment)
                throws TTransportException {
            TSimpleServer server = new TSimpleServer(new TSimpleServer.Args(new TServerSocket(environment.getPort()))
                    .processor(getProcessor()).inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer<TSimpleServer>(server, environment) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.Client(environment);
                }

                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static SyncEchoTestServer<TThreadPoolServer> threadedPoolServer(final TestEnvironment environment)
                throws TTransportException {
            TThreadPoolServer server = new TThreadPoolServer(new TThreadPoolServer.Args(new TServerSocket(
                    environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer<TThreadPoolServer>(server, environment) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.Client(environment);
                }

                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static SyncEchoTestServer<TThreadedSelectorServer> threadedSelectorServer(
                final TestEnvironment environment) throws TTransportException {
            TThreadedSelectorServer server = new TThreadedSelectorServer(new TThreadedSelectorServer.Args(
                    new TNonblockingServerSocket(environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer<TThreadedSelectorServer>(server, environment) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer(environment);
                }

                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static SyncEchoTestServer<TNonblockingServer> nonblockingServer(final TestEnvironment environment)
                throws TTransportException {
            TNonblockingServer server = new TNonblockingServer(new TNonblockingServer.Args(
                    new TNonblockingServerSocket(environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer<TNonblockingServer>(server, environment) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer(environment);
                }

                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }

        public static SyncEchoTestServer<THsHaServer> halfSyncHalfAsyncServer(final TestEnvironment environment)
                throws TTransportException {
            THsHaServer server = new THsHaServer(new THsHaServer.Args(new TNonblockingServerSocket(
                    environment.getPort())).processor(getProcessor())
                    .inputProtocolFactory(environment.getProtocolFactory())
                    .outputProtocolFactory(environment.getProtocolFactory()));
            return new SyncEchoTestServer<THsHaServer>(server, environment) {
                @Override
                public SyncEchoTestClient getSynchronousClient() throws TTransportException {
                    return new SyncEchoTestClient.ClientForNonblockingServer(environment);
                }

                @Override
                public AsyncEchoTestClient getAsynchronousClient() throws IOException {
                    return new AsyncEchoTestClient.Client(environment);
                }
            };
        }
    }
}
