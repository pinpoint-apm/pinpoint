/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTraceField;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Taejin Koo
 */
public abstract class GrpcITBase {

    private final Logger logger = Logger.getLogger(Grpc_1_21_0_to_IT.class.getName());

    private final String REQUEST = "hello";

    @Test
    public void requestResponseTest() throws Exception {
        HelloWorldSimpleServer server = null;
        HelloWorldSimpleClient client = null;
        try {
            server = new HelloWorldSimpleServer();
            server.start();

            client = new HelloWorldSimpleClient("127.0.0.1", server.getBindPort());
            String response = client.greet(REQUEST);
            Assert.assertEquals(REQUEST.toUpperCase(), response);

            PluginTestVerifier verifier = getPluginTestVerifier();

            assertTrace(server, verifier);

            verifier.awaitTraceCount(getExpectedRequestResponseTestTraceCount(), 20, 3000);
            verifier.verifyTraceCount(getExpectedRequestResponseTestTraceCount());
        } finally {
            clearResources(client, server);
        }
    }

    protected int getExpectedRequestResponseTestTraceCount() {
        return 8;
    }

    @Test
    public void streamingTest() throws Exception {
        HelloWorldStreamServer server = null;
        HelloWorldStreamClient client = null;

        Random random = new Random(System.currentTimeMillis());
        int requestCount = random.nextInt(5) + 1;

        try {
            server = new HelloWorldStreamServer();
            server.start();

            client = new HelloWorldStreamClient("127.0.0.1", server.getBindPort());
            client.greet(requestCount);
            Assert.assertEquals(requestCount, server.getRequestCount());

            PluginTestVerifier verifier = getPluginTestVerifier();

            assertTrace(server, verifier);

            verifier.awaitTraceCount(6 + (requestCount * 2), 20, 3000);
            verifier.verifyTraceCount(6 + (requestCount * 2));
        } finally {
            clearResources(client, server);
        }
    }

    private PluginTestVerifier getPluginTestVerifier() {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        if (logger.isLoggable(Level.FINE)) {
            verifier.printCache();
        }
        return verifier;
    }


    private void assertTrace(HelloWorldServer server, PluginTestVerifier verifier) {
        verifier.verifyTrace(clientCallStartEvent(server));
        verifier.verifyTrace(event("GRPC_INTERNAL", "io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl()"));

        verifier.verifyTrace(createServerRootTrace(server));

        String streacmCreatedMethodDescritor = "io.grpc.internal.ServerImpl$ServerTransportListenerImpl.streamCreated(io.grpc.internal.ServerStream, java.lang.String, io.grpc.Metadata)";
        verifier.verifyTrace(event("GRPC_SERVER_INTERNAL", streacmCreatedMethodDescritor));
    }

    private ExpectedTrace clientCallStartEvent(HelloWorldServer server) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder("GRPC");
        eventBuilder.setMethodSignature("io.grpc.internal.ClientCallImpl.start(io.grpc.ClientCall$Listener, io.grpc.Metadata)");

        String remoteAddress = "127.0.0.1:" + server.getBindPort();
        eventBuilder.setEndPoint(remoteAddress);
        eventBuilder.setDestinationId(remoteAddress);
        eventBuilder.setAnnotations(annotation("http.url", "http://" + remoteAddress + "/" + server.getMethodName()));

        return eventBuilder.build();
    }

    private ExpectedTrace createServerRootTrace(HelloWorldServer server) {
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder("GRPC_SERVER");
        rootBuilder.setMethodSignature("gRPC HTTP Server");
        rootBuilder.setRpc("/" + server.getMethodName());
        rootBuilder.setRemoteAddr(ExpectedTraceField.createStartWith("127.0.0.1:"));
        return rootBuilder.build();
    }

    private void clearResources(HelloWorldClient client, HelloWorldServer server) {
        try {
            if (client != null) {
                client.shutdown();
            }
        } catch (Exception e) {
        }
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
        }
    }

}
