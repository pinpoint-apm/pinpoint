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

package com.pinpoint.test.plugin.client;

import com.pinpoint.test.plugin.TestEnvironment;
import com.pinpoint.test.plugin.dto.EchoService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public abstract class SyncEchoTestClient implements EchoTestClient {
    private final TestEnvironment environment;
    private final TTransport transport;

    private SyncEchoTestClient(TestEnvironment environment, TTransport transport) throws TTransportException {
        this.environment = environment;
        this.transport = transport;
        this.transport.open();
    }

    @Override
    public final String echo(String message) throws TException {
        TProtocol protocol = this.environment.getProtocolFactory().getProtocol(transport);
        EchoService.Client client = new EchoService.Client(protocol);
        return client.echo(message);
    }

    @Override
    public void close() {
        if (this.transport.isOpen()) {
            this.transport.close();
        }
    }

    public static class Client extends SyncEchoTestClient {
        public Client(TestEnvironment environment) throws TTransportException {
            super(environment, new TSocket(environment.getServerIp(), environment.getPort()));
        }
    }

    public static class ClientForNonblockingServer extends SyncEchoTestClient {
        public ClientForNonblockingServer(TestEnvironment environment) throws TTransportException {
            super(environment, new TFramedTransport(new TSocket(environment.getServerIp(), environment.getPort())));
        }
    }
}
