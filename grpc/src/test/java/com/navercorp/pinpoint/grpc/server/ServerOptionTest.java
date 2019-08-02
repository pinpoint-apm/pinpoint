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

package com.navercorp.pinpoint.grpc.server;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServerOptionTest {

    @Test
    public void build() throws Exception {
        ServerOption.Builder builder = new ServerOption.Builder();
        builder.setKeepAliveTime(1);
        builder.setKeepAliveTimeout(1);
        builder.setPermitKeepAliveTime(1);

        builder.setMaxConnectionIdle(1);

        builder.setMaxConcurrentCallsPerConnection(1);
        builder.setFlowControlWindow(1);
        builder.setMaxHeaderListSize(1);

        builder.setReceiveBufferSize(1);

        builder.setHandshakeTimeout(1);
        builder.setMaxInboundMessageSize(1);


        ServerOption serverOption = builder.build();
        assertEquals(1, serverOption.getKeepAliveTime());
        assertEquals(1, serverOption.getKeepAliveTimeout());
        assertEquals(1, serverOption.getPermitKeepAliveTime());


        assertEquals(1, serverOption.getMaxConnectionIdle());

        assertEquals(1, serverOption.getMaxConcurrentCallsPerConnection());
        assertEquals(1, serverOption.getFlowControlWindow());
        assertEquals(1, serverOption.getMaxHeaderListSize());

        assertEquals(1, serverOption.getReceiveBufferSize());

        assertEquals(1, serverOption.getHandshakeTimeout());
        assertEquals(1, serverOption.getMaxInboundMessageSize());
    }
}