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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CountDownLatch;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.async.TAsyncMethodCall;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;

/**
 * @author HyunGil Jeong
 */
public class AsyncEchoTestClient implements EchoTestClient {

    private final TestEnvironment environment;
    private final TNonblockingTransport transport;
    private final EchoService.AsyncClient asyncClient;
    private final TAsyncClientManager asyncClientManager = new TAsyncClientManager();

    private AsyncEchoTestClient(TestEnvironment environment) throws IOException {
        this.environment = environment;
        this.transport = new TNonblockingSocket(this.environment.getServerIp(), this.environment.getPort());
        this.asyncClient = new EchoService.AsyncClient(this.environment.getProtocolFactory(), this.asyncClientManager, this.transport);
    }

    @Override
    public String echo(String message) throws TException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AsyncEchoResultHolder resultHolder = new AsyncEchoResultHolder();
        final AsyncMethodCallback<String> callback = new EchoMethodCallback(latch, resultHolder);
        this.asyncClient.echo(message, callback);
        boolean isInterrupted = false;
        while (true) {
            try {
                latch.await();
                return resultHolder.getResult();
            } catch (InterruptedException e) {
                isInterrupted = true;
            } finally {
                if (isInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void verifyTraces(PluginTestVerifier verifier, String expectedMessage) throws Exception {
        final InetSocketAddress actualServerAddress = this.environment.getServerAddress();
        // ********** Asynchronous Traces
        // SpanEvent - Asynchronous Invocation
        ExpectedTrace asyncInvocationTrace = event("ASYNC", "Asynchronous Invocation");

        // SpanEvent - TAsyncMethodCall.cleanUpAndFireCallback
        Method cleanUpAndFireCallback = TAsyncMethodCall.class.getDeclaredMethod("cleanUpAndFireCallback",
                SelectionKey.class);
        ExpectedTrace cleanUpAndFireCallbackTrace = event("THRIFT_CLIENT_INTERNAL", cleanUpAndFireCallback);

        // SpanEvent - TServiceClient.receiveBase
        Method receiveBase = TServiceClient.class.getDeclaredMethod("receiveBase", TBase.class, String.class);
        ExpectedAnnotation thriftResult = Expectations.annotation("thrift.result", "echo_result(success:"
                + expectedMessage + ")");
        ExpectedTrace receiveBaseTrace = event("THRIFT_CLIENT_INTERNAL", // ServiceType
                receiveBase, // Method
                thriftResult // Annotation("thrift.result")
        );

        // ********** Root trace for Asynchronous traces
        // SpanEvent - TAsyncClientManager.call
        Method call = TAsyncClientManager.class.getDeclaredMethod("call", TAsyncMethodCall.class);
        ExpectedAnnotation thriftUrl = Expectations.annotation("thrift.url", actualServerAddress.getHostName() + ":"
                + actualServerAddress.getPort() + "/com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo_call");
        ExpectedTrace callTrace = event("THRIFT_CLIENT", // ServiceType
                call, // Method
                null, // rpc
                null, // endPoint
                actualServerAddress.getHostName() + ":" + actualServerAddress.getPort(), // destinationId
                thriftUrl // Annotation("thrift.url")
        );
        verifier.verifyTrace(async(callTrace, asyncInvocationTrace, cleanUpAndFireCallbackTrace, receiveBaseTrace));
    }

    private static class AsyncEchoResultHolder {
        private volatile String result;

        public void setResult(String result) {
            this.result = result;
        }

        public String getResult() {
            return this.result;
        }
    }

    @Override
    public void close() {
        if (this.asyncClientManager.isRunning()) {
            this.asyncClientManager.stop();
        }
        if (this.transport.isOpen()) {
            this.transport.close();
        }
    }

    private static class EchoMethodCallback implements AsyncMethodCallback<String> {

        private final CountDownLatch completeLatch;
        private final AsyncEchoResultHolder resultHolder;

        private EchoMethodCallback(final CountDownLatch completeLatch, final AsyncEchoResultHolder resultHolder) {
            this.completeLatch = completeLatch;
            this.resultHolder = resultHolder;
        }

        @Override
        public void onComplete(String response) {
            this.resultHolder.setResult(response);
            this.completeLatch.countDown();
        }

        @Override
        public void onError(Exception exception) {
            try {
                this.resultHolder.setResult(exception.toString());
            } finally {
                this.completeLatch.countDown();
            }
        }

    }

    public static class Client extends AsyncEchoTestClient {
        public Client(TestEnvironment environment) throws IOException {
            super(environment);
        }
    }

}
