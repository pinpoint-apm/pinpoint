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

package com.navercorp.pinpoint.plugin.thrift.common.client;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.lang.reflect.Method;

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
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;

/**
 * @author HyunGil Jeong
 */
public abstract class SyncEchoTestClient implements EchoTestClient {
    
    private final TTransport transport;
    
    private SyncEchoTestClient(TTransport transport) throws TTransportException {
        this.transport = transport;
        this.transport.open();
    }
    
    @Override
    public final String echo(String message) throws TException {
        TProtocol protocol = PROTOCOL_FACTORY.getProtocol(transport);
        EchoService.Client client = new EchoService.Client(protocol);
        return client.echo(message);
    }
    
    @Override
    public void verifyTraces(PluginTestVerifier verifier, String expectedMessage) throws Exception {
        // SpanEvent - TServiceClient.sendBase
        Method sendBase = TServiceClient.class.getDeclaredMethod("sendBase", String.class, TBase.class);
        // refer to com.navercorp.pinpoint.plugin.thrift.ThriftUtils#getClientServiceName
        ExpectedAnnotation thriftUrl = Expectations.annotation("thrift.url",
                SERVER_ADDRESS.getHostName() + ":" + SERVER_ADDRESS.getPort() + "/com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo");
        ExpectedAnnotation thriftArgs = Expectations.annotation("thrift.args", "echo_args(message:" + expectedMessage + ")");
        verifier.verifyTrace(event(
                "THRIFT_CLIENT", // ServiceType
                sendBase, // Method
                null, // rpc
                null, // endPoint
                SERVER_ADDRESS.getHostName() + ":" + SERVER_ADDRESS.getPort(), // destinationId
                thriftUrl, // Annotation("thrift.url")
                thriftArgs // Annotation("thrift.args")
        ));

        // SpanEvent - TServiceClient.receiveBase
        Method receiveBase = TServiceClient.class.getDeclaredMethod("receiveBase", TBase.class, String.class);
        ExpectedAnnotation thriftResult = Expectations.annotation("thrift.result", "echo_result(success:" + expectedMessage + ")");
        verifier.verifyTrace(event(
                "THRIFT_CLIENT_INTERNAL", // ServiceType
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
        public Client() throws TTransportException {
            super(new TSocket(SERVER_IP, SERVER_PORT));
        }
    }
    
    public static class ClientForNonblockingServer extends SyncEchoTestClient {
        public ClientForNonblockingServer() throws TTransportException {
            super(new TFramedTransport(new TSocket(SERVER_IP, SERVER_PORT)));
        }
    }
}
