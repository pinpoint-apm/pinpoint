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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Taejin Koo
 */
public class UnaryCallDeadlineInterceptorTest {

    @Test
    public void withDeadlineTest() {
        Channel channel = new TestChannel();

        UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(5000);

        ClientCall<PAgentInfo, PResult> pAgentInfoPResultClientCall = unaryCallDeadlineInterceptor.interceptCall(createMethodDescritor(MethodDescriptor.MethodType.UNARY), CallOptions.DEFAULT.withAuthority("test"), channel);

        Assert.assertNotNull(((TestChannel) channel).callOptions.getDeadline());
    }

    @Test
    public void withoutDeadlineTest() {
        Channel channel = new TestChannel();

        UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(5000);

        unaryCallDeadlineInterceptor.interceptCall(createMethodDescritor(MethodDescriptor.MethodType.BIDI_STREAMING), CallOptions.DEFAULT.withAuthority("test"), channel);

        Assert.assertNull(((TestChannel) channel).callOptions.getDeadline());
    }

    private MethodDescriptor createMethodDescritor(MethodDescriptor.MethodType methodType) {
        return MethodDescriptor.<Integer, Integer>newBuilder()
                .setType(methodType)
                .setFullMethodName("test/method")
                .setRequestMarshaller(Mockito.mock(MethodDescriptor.Marshaller.class))
                .setResponseMarshaller(Mockito.mock(MethodDescriptor.Marshaller.class))
                .build();


    }

    private static class TestChannel extends Channel {

        private CallOptions callOptions;

        @Override
        public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
            this.callOptions = callOptions;
            return null;
        }

        @Override
        public String authority() {
            return null;
        }

    }

}
