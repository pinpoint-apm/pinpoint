/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.client;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ClientOptionTest {

    @Test
    public void build() throws Exception {
        ClientOption.Builder clientOptionBuilder = new ClientOption.Builder();
        clientOptionBuilder.setKeepAliveTime(1);
        clientOptionBuilder.setKeepAliveTimeout(1);

        clientOptionBuilder.setFlowControlWindow(65535);
        clientOptionBuilder.setMaxHeaderListSize(1);

        clientOptionBuilder.setMaxInboundMessageSize(1);
        clientOptionBuilder.setConnectTimeout(1);

        clientOptionBuilder.setWriteBufferHighWaterMark(1);
        clientOptionBuilder.setWriteBufferLowWaterMark(1);

        ClientOption clientOption = clientOptionBuilder.build();
        assertEquals(1, clientOption.getKeepAliveTime());
        assertEquals(1, clientOption.getKeepAliveTimeout());
        assertEquals(false, clientOption.isKeepAliveWithoutCalls());
        assertEquals(TimeUnit.DAYS.toMillis(30), clientOption.getIdleTimeoutMillis());
        assertEquals(65535, clientOption.getFlowControlWindow());
        assertEquals(1, clientOption.getMaxHeaderListSize());
        assertEquals(1, clientOption.getMaxInboundMessageSize());
        assertEquals(1, clientOption.getConnectTimeout());
        assertEquals(1, clientOption.getWriteBufferHighWaterMark());
        assertEquals(1, clientOption.getWriteBufferLowWaterMark());
    }
}