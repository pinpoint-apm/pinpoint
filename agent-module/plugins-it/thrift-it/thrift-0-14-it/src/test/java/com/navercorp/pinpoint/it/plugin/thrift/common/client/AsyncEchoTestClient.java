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

package com.navercorp.pinpoint.it.plugin.thrift.common.client;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.it.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.it.plugin.thrift.dto.EchoService;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.async.TAsyncMethodCall;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.async;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class AsyncEchoTestClient implements EchoTestClient {

    private static final long AWAIT_TIMEOUT_SECONDS = 5L;

    private final TestEnvironment environment;
    private final TNonblockingTransport transport;
    private final EchoService.AsyncClient asyncClient;
    private final TAsyncClientManager asyncClientManager = new TAsyncClientManager();

    private AsyncEchoTestClient(TestEnvironment environment) throws Exception {
        this.environment = environment;
        this.transport = new TNonblockingSocket(this.environment.getServerIp(), this.environment.getPort());
        this.asyncClient = new EchoService.AsyncClient(this.environment.getProtocolFactory(), this.asyncClientManager, this.transport);
        System.out.println("##AsyncEchoTestClient ip=" + environment.getServerIp() + ", port=" + environment.getPort());
    }

    @Override
    public String echo(String message) throws TException {
        final FutureCallback<String> callback = new FutureCallback<>();
        this.asyncClient.echo(message, callback);
        return await(callback.future());
    }

    private static <T> T await(CompletableFuture<T> future) throws TException {
        try {
            return future.get(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TException("Interrupted while waiting for response", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TException) {
                throw (TException) cause;
            }
            throw new TException(cause);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TException("Timed out waiting for response after " + AWAIT_TIMEOUT_SECONDS + "s", e);
        }
    }

    @Override
    public void verifyTraces(PluginTestVerifier verifier, String expectedMessage) throws Exception {
        final InetSocketAddress socketAddress = this.environment.getServerAddress();
        final String hostName = SocketAddressUtils.getHostNameFirst(socketAddress);
        // refer to com.navercorp.pinpoint.plugin.thrift.ThriftUtils#getHostPort
        final String remoteAddress = HostAndPort.toHostAndPortString(hostName, socketAddress.getPort());
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
        ExpectedAnnotation thriftUrl = Expectations.annotation("thrift.url",
                remoteAddress + "/com/navercorp/pinpoint/it/plugin/thrift/dto/EchoService/echo");
        ExpectedTrace callTrace = event("THRIFT_CLIENT", // ServiceType
                call, // Method
                null, // rpc
                null, // endPoint
                remoteAddress, // destinationId
                thriftUrl // Annotation("thrift.url")
        );
        verifier.verifyTrace(async(callTrace, asyncInvocationTrace, cleanUpAndFireCallbackTrace, receiveBaseTrace));
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

    private static class FutureCallback<T> implements AsyncMethodCallback<T> {

        private final CompletableFuture<T> future = new CompletableFuture<>();

        @Override
        public void onComplete(T response) {
            this.future.complete(response);
        }

        @Override
        public void onError(Exception exception) {
            this.future.completeExceptionally(exception);
        }

        public CompletableFuture<T> future() {
            return this.future;
        }
    }

    public static class Client extends AsyncEchoTestClient {
        public Client(TestEnvironment environment) throws Exception {
            super(environment);
        }
    }

}
