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
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author HyunGil Jeong
 */
public class AsyncEchoTestClient implements EchoTestClient {

    private static final long AWAIT_TIMEOUT_SECONDS = 5L;

    private final TNonblockingTransport transport;
    private final EchoService.AsyncClient asyncClient;
    private final TAsyncClientManager asyncClientManager = new TAsyncClientManager();

    private AsyncEchoTestClient(TestEnvironment environment) throws IOException {
        Objects.requireNonNull(environment, "environment");
        try {
            this.transport = new TNonblockingSocket(environment.getServerIp(), environment.getPort());
        } catch (TTransportException e) {
            throw new IOException("Failed to create non-blocking transport", e);
        }
        this.asyncClient = new EchoService.AsyncClient(environment.getProtocolFactory(), this.asyncClientManager, this.transport);
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
        public Client(TestEnvironment environment) throws IOException {
            super(environment);
        }
    }

}
