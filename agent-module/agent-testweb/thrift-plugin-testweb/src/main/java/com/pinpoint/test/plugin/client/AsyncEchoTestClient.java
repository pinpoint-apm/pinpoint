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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

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
