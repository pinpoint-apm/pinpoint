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

package com.navercorp.pinpoint.plugin.thrift.common.client;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;

/**
 * @author HyunGil Jeong
 */
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
    public void verifyTraces(PluginTestVerifier verifier, String expectedMessage) throws Exception {
        // refer to TServiceClientSendBaseInterceptor.getRemoteAddress(...)
        final InetSocketAddress socketAddress = this.environment.getServerAddress();
        final String hostName = SocketAddressUtils.getHostNameFirst(socketAddress);
        final String remoteAddress = HostAndPort.toHostAndPortString(hostName, socketAddress.getPort());

        // SpanEvent - TServiceClient.sendBase
        Method sendBase = TServiceClient.class.getDeclaredMethod("sendBase", String.class, TBase.class);

        ExpectedAnnotation thriftUrl = Expectations.annotation("thrift.url",
                remoteAddress + "/com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo");
        ExpectedAnnotation thriftArgs = Expectations.annotation("thrift.args",
                "echo_args(message:" + expectedMessage + ")");

        // SpanEvent - TServiceClient.receiveBase
        Method receiveBase = TServiceClient.class.getDeclaredMethod("receiveBase", TBase.class, String.class);
        ExpectedAnnotation thriftResult = Expectations.annotation("thrift.result", "echo_result(success:"
                + expectedMessage + ")");

        verifier.verifyDiscreteTrace(event("THRIFT_CLIENT", // ServiceType
                sendBase, // Method
                null, // rpc
                null, // endPoint
                remoteAddress, // destinationId
                thriftUrl, // Annotation("thrift.url")
                thriftArgs), // Annotation("thrift.args")
                event("THRIFT_CLIENT_INTERNAL", // ServiceType
                        receiveBase, // Method
                        thriftResult // Annotation("thrift.result")
                ));
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
