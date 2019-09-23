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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class SpanReceiverConfigurationTest {

    @Test
    public void properties() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-pinpoint-collector.properties");
        SpanReceiverConfiguration configuration = new SpanReceiverConfiguration(properties, new DeprecatedConfiguration());

        assertEquals(Boolean.FALSE, configuration.isGrpcEnable());
        assertEquals("3.3.3.3", configuration.getGrpcBindIp());
        assertEquals(3, configuration.getGrpcBindPort());
        assertEquals(3, configuration.getGrpcWorkerExecutorThreadSize());
        assertEquals(3, configuration.getGrpcWorkerExecutorQueueSize());
        assertEquals(Boolean.FALSE, configuration.isGrpcWorkerExecutorMonitorEnable());
        assertEquals(3, configuration.getGrpcServerOption().getKeepAliveTime());
        assertEquals(3, configuration.getGrpcServerOption().getKeepAliveTimeout());
        assertEquals(3, configuration.getGrpcServerOption().getPermitKeepAliveTime());
        assertEquals(3, configuration.getGrpcServerOption().getMaxConnectionIdle());
        assertEquals(3, configuration.getGrpcServerOption().getMaxConcurrentCallsPerConnection());
        // 3M
        assertEquals(3 * 1024 * 1024, configuration.getGrpcServerOption().getMaxInboundMessageSize());
        // 3K
        assertEquals(3 * 1024, configuration.getGrpcServerOption().getMaxHeaderListSize());
        // 3M
        assertEquals(3 * 1024 * 1024, configuration.getGrpcServerOption().getFlowControlWindow());

        assertEquals(3, configuration.getGrpcServerOption().getHandshakeTimeout());
        // 3M
        assertEquals(3 * 1024 * 1024, configuration.getGrpcServerOption().getReceiveBufferSize());
    }
}