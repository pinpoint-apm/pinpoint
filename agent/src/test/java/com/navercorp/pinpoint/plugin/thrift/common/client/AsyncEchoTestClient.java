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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService;
import com.navercorp.pinpoint.plugin.thrift.dto.EchoService.AsyncClient.echo_call;

/**
 * @author HyunGil Jeong
 */
public class AsyncEchoTestClient implements EchoTestClient {
    
    private final TNonblockingTransport transport;
    private final EchoService.AsyncClient asyncClient;
    private final TAsyncClientManager asyncClientManager = new TAsyncClientManager();
    
    private AsyncEchoTestClient(TNonblockingTransport transport) throws IOException {
        this.transport = transport;
        this.asyncClient = new EchoService.AsyncClient(PROTOCOL_FACTORY, this.asyncClientManager, this.transport);
    }

    @Override
    public String echo(String message) throws TException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AsyncEchoResultHolder resultHolder = new AsyncEchoResultHolder();
        final AsyncMethodCallback<EchoService.AsyncClient.echo_call> callback = new EchoMethodCallback(latch, resultHolder);
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
        
        // ********** Asynchronous Traces
        // SpanEvent - Thrift Asynchronous Client Invocation
        ExpectedTrace asyncClientInvocationTrace = event("ASYNC", "Thrift Asynchronous Client Invocation");
        
        // SpanEvent - TAsyncMethodCall.start
        Method start = TAsyncMethodCall.class.getDeclaredMethod("start", Selector.class);
        ExpectedTrace startTrace = event("THRIFT_CLIENT_INTERNAL", start);
        
        // SpanEvent - TAsyncMethodCall.doConnecting
        Method doConnecting = TAsyncMethodCall.class.getDeclaredMethod("doConnecting", SelectionKey.class);
        ExpectedTrace doConnectingTrace = event("THRIFT_CLIENT_INTERNAL", doConnecting);
        
        // SpanEvent - TAsyncMethodCall.doWritingRequestSize
        Method doWritingRequestSize = TAsyncMethodCall.class.getDeclaredMethod("doWritingRequestSize");
        ExpectedTrace doWritingRequestSizeTrace = event("THRIFT_CLIENT_INTERNAL", doWritingRequestSize);
        
        // SpanEvent - TAsyncMethodCall.doWritingRequestBody
        Method doWritingRequestBody = TAsyncMethodCall.class.getDeclaredMethod("doWritingRequestBody", SelectionKey.class);
        ExpectedAnnotation thriftUrl = Expectations.annotation(
                "thrift.url", SERVER_ADDRESS.getHostName() + ":" + SERVER_ADDRESS.getPort() + "/com/navercorp/pinpoint/plugin/thrift/dto/EchoService/echo_call");
        ExpectedTrace doWritingRequestBodyTrace = event(
                "THRIFT_CLIENT", // ServiceType
                doWritingRequestBody, // Method
                null, // rpc
                null, // endPoint
                SERVER_ADDRESS.getHostName() + ":" + SERVER_ADDRESS.getPort(), // destinationId
                thriftUrl // Annotation("thrift.url")
        );
        
        // SpanEvent - TAsyncMethodCall.doReadingResponseSize
        Method doReadingResponseSize = TAsyncMethodCall.class.getDeclaredMethod("doReadingResponseSize");
        ExpectedTrace doReadingResponseSizeTrace = event("THRIFT_CLIENT_INTERNAL", doReadingResponseSize);
        
        // SpanEvent - TAsyncMethodCall.doReadingResponseBody
        Method doReadingResponseBody = TAsyncMethodCall.class.getDeclaredMethod("doReadingResponseBody", SelectionKey.class);
        ExpectedTrace doReadingResponseBodyTrace = event("THRIFT_CLIENT_INTERNAL", doReadingResponseBody);
        
        // SpanEvent - TAsyncMethodCall.cleanUpAndFireCallback
        Method cleanUpAndFireCallback = TAsyncMethodCall.class.getDeclaredMethod("cleanUpAndFireCallback", SelectionKey.class);
        ExpectedTrace cleanUpAndFireCallbackTrace = event("THRIFT_CLIENT_INTERNAL", cleanUpAndFireCallback);
        
        // SpanEvent - TServiceClient.receiveBase
        Method receiveBase = TServiceClient.class.getDeclaredMethod("receiveBase", TBase.class, String.class);
        ExpectedAnnotation thriftResult = Expectations.annotation("thrift.result", "echo_result(success:" + expectedMessage + ")");
        ExpectedTrace receiveBaseTrace = event(
                "THRIFT_CLIENT_INTERNAL", // ServiceType
                receiveBase, // Method
                thriftResult // Annotation("thrift.result")
        );
        
        // ********** Root trace for Asynchronous traces
        // SpanEvent - TAsyncClientManager.call
        Method call = TAsyncClientManager.class.getDeclaredMethod("call", TAsyncMethodCall.class);
        verifier.verifyTrace(async("THRIFT_CLIENT_INTERNAL", call, null,
                                        asyncClientInvocationTrace,
                                        startTrace,
                                        doConnectingTrace,
                                        doWritingRequestSizeTrace,
                                        doWritingRequestBodyTrace,
                                        doReadingResponseSizeTrace,
                                        doReadingResponseBodyTrace,
                                        cleanUpAndFireCallbackTrace,
                                        receiveBaseTrace));
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
    
    private static class EchoMethodCallback implements AsyncMethodCallback<EchoService.AsyncClient.echo_call> {
        
        private final CountDownLatch completeLatch;
        private final AsyncEchoResultHolder resultHolder;
        
        private EchoMethodCallback(final CountDownLatch completeLatch, final AsyncEchoResultHolder resultHolder) {
            this.completeLatch = completeLatch;
            this.resultHolder = resultHolder;
        }

        @Override
        public void onComplete(echo_call response) {
            try {
                String result = response.getResult();
                this.resultHolder.setResult(result);
            } catch (TException e) {
                this.resultHolder.setResult(e.toString());
            } finally {
                this.completeLatch.countDown();
            }
            
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
        public Client() throws IOException {
            super(new TNonblockingSocket(SERVER_IP, SERVER_PORT));
        }
    }

}
