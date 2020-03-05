/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class DiscardClientInterceptorTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MethodDescriptor<String, Integer> descriptor;
    @Mock
    private MethodDescriptor.Marshaller<String> stringMarshaller;

    @Mock
    private MethodDescriptor.Marshaller<Integer> intMarshaller;

    private CallOptions callOptions;

    private ClientCallRecorder clientCall;

    @Mock
    private Channel channel;

    private DiscardEventListener<String> discardEventListener;

    @Mock
    private ClientCall.Listener<Integer> listener;

    private DiscardClientInterceptor interceptor;

    private DiscardClientInterceptor.DiscardClientCall<String, Integer> call ;

    @Before
    public void setUp() throws Exception {
        this.descriptor = MethodDescriptor.<String, Integer>newBuilder()
                .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
                .setFullMethodName("a.service/method")
                .setRequestMarshaller(stringMarshaller)
                .setResponseMarshaller(intMarshaller)
                .build();
        this.callOptions = CallOptions.DEFAULT;
        this.clientCall = new ClientCallRecorder();
        when(channel.newCall(descriptor, callOptions)).thenReturn(clientCall);

        discardEventListener = spy(new LoggingDiscardEventListener<String>(DiscardClientInterceptorTest.class.getName(), 1));
        this.interceptor = new DiscardClientInterceptor(discardEventListener, 1);

        this.call = (DiscardClientInterceptor.DiscardClientCall<String, Integer>) interceptor.interceptCall(descriptor, callOptions, channel);
    }

    @Test
    public void interceptCall_success() {
        call.start(listener, new Metadata());
        clientCall.responseListener.onReady();
        clientCall.isReady = true;

        call.sendMessage("test");
        Assert.assertTrue(call.getOnReadyState());
        verify(discardEventListener, never()).onDiscard(anyString());
    }

    @Test
    public void interceptCall_not_ready() {
        call.start(listener, new Metadata());

        clientCall.responseListener.onReady();
        clientCall.isReady = false;

        call.sendMessage("test");

        Assert.assertTrue(call.getOnReadyState());
        verify(discardEventListener).onDiscard(anyString());
    }

    @Test
    public void interceptCall_pending_queue() {

        call.sendMessage("test");
        Assert.assertFalse(call.getOnReadyState());
        verify(discardEventListener, never()).onDiscard(anyString());
        Assert.assertEquals(call.getPendingCount(), 1);
    }

    @Test
    public void interceptCall_pending_fail() {

        call.sendMessage("test");
        call.sendMessage("test");

        Assert.assertFalse(call.getOnReadyState());
        verify(discardEventListener).onDiscard(anyString());
        Assert.assertEquals(call.getPendingCount(), 2);
    }

    private static class ClientCallRecorder extends ClientCall<String, Integer> {
        boolean isReady;
        Listener<Integer> responseListener;
        @Override
        public boolean isReady() {
            return isReady;
        }

        @Override
        public void setMessageCompression(boolean enabled) {

        }

        @Override
        public Attributes getAttributes() {
            return null;
        }

        @Override
        public void start(Listener<Integer> responseListener, Metadata headers) {
            this.responseListener = responseListener;
        }

        @Override
        public void request(int numMessages) {

        }

        @Override
        public void cancel(String message, Throwable cause) {

        }

        @Override
        public void halfClose() {

        }

        @Override
        public void sendMessage(String message) {

        }
    }

}