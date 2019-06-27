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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author jaehong.kim
 */
public class GrpcTransportConfigTest {

    @Test
    public void read() throws Exception {
        // Mock
        String path = GrpcTransportConfigTest.class.getResource("/pinpoint.config").getPath();
        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);

        GrpcTransportConfig config = new GrpcTransportConfig();
        config.read(profilerConfig);

        // Agent
        assertEquals("127.0.0.1", config.getAgentCollectorIp());
        assertEquals(1, config.getAgentCollectorPort());
        assertEquals(1, config.getAgentRequestTimeout());
        assertEquals(1, config.getAgentChannelExecutorQueueSize());

        assertEquals(1, config.getAgentClientOption().getKeepAliveTime());
        assertEquals(1, config.getAgentClientOption().getKeepAliveTimeout());
        assertEquals(1, config.getAgentClientOption().getConnectTimeout());
        assertEquals(1048576, config.getAgentClientOption().getMaxHeaderListSize());
        assertEquals(1048576, config.getAgentClientOption().getMaxInboundMessageSize());
        assertEquals(1048576, config.getAgentClientOption().getFlowControlWindow());
        assertEquals(1048576, config.getAgentClientOption().getWriteBufferHighWaterMark());
        assertEquals(1048576, config.getAgentClientOption().getWriteBufferLowWaterMark());

        assertEquals("127.0.0.1", config.getStatCollectorIp());
        assertEquals(2, config.getStatCollectorPort());
        assertEquals(2, config.getStatRequestTimeout());
        assertEquals(2, config.getStatChannelExecutorQueueSize());
        assertEquals(2, config.getStatSenderExecutorQueueSize());

        assertEquals(2, config.getStatClientOption().getKeepAliveTime());
        assertEquals(2, config.getStatClientOption().getKeepAliveTimeout());
        assertEquals(2, config.getStatClientOption().getConnectTimeout());
        assertEquals(2097152, config.getStatClientOption().getMaxHeaderListSize());
        assertEquals(2097152, config.getStatClientOption().getMaxInboundMessageSize());
        assertEquals(2097152, config.getStatClientOption().getFlowControlWindow());
        assertEquals(2097152, config.getStatClientOption().getWriteBufferHighWaterMark());
        assertEquals(2097152, config.getStatClientOption().getWriteBufferLowWaterMark());


        assertEquals("127.0.0.1", config.getSpanCollectorIp());
        assertEquals(3, config.getSpanCollectorPort());
        assertEquals(3, config.getSpanRequestTimeout());
        assertEquals(3, config.getSpanChannelExecutorQueueSize());
        assertEquals(3, config.getSpanSenderExecutorQueueSize());

        assertEquals(3, config.getSpanClientOption().getKeepAliveTime());
        assertEquals(3, config.getSpanClientOption().getKeepAliveTimeout());
        assertEquals(3, config.getSpanClientOption().getConnectTimeout());
        assertEquals(3145728, config.getSpanClientOption().getMaxHeaderListSize());
        assertEquals(3145728, config.getSpanClientOption().getMaxInboundMessageSize());
        assertEquals(3145728, config.getSpanClientOption().getFlowControlWindow());
        assertEquals(3145728, config.getSpanClientOption().getWriteBufferHighWaterMark());
        assertEquals(3145728, config.getSpanClientOption().getWriteBufferLowWaterMark());

        System.out.println(config);
    }
}