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

public class StatReceiverConfigurationTest {

    @Test
    public void properties() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-pinpoint-collector.properties");
        StatReceiverConfiguration configuration = new StatReceiverConfiguration(properties, new DeprecatedConfiguration());

        assertEquals(Boolean.FALSE, configuration.isGrpcEnable());
        assertEquals("2.2.2.2", configuration.getGrpcBindIp());
        assertEquals(2, configuration.getGrpcBindPort());
        assertEquals(2, configuration.getGrpcWorkerThreadSize());
        assertEquals(2, configuration.getGrpcWorkerQueueSize());
        assertEquals(Boolean.FALSE, configuration.isGrpcWorkerMonitorEnable());
        assertEquals(2, configuration.getGrpcServerOption().getKeepAliveTime());
        assertEquals(2, configuration.getGrpcServerOption().getKeepAliveTimeout());
        assertEquals(2, configuration.getGrpcServerOption().getPermitKeepAliveTime());
        assertEquals(2, configuration.getGrpcServerOption().getMaxConnectionIdle());
        assertEquals(2, configuration.getGrpcServerOption().getMaxConcurrentCallsPerConnection());
        assertEquals(2, configuration.getGrpcServerOption().getMaxInboundMessageSize());
        assertEquals(2, configuration.getGrpcServerOption().getMaxHeaderListSize());
        assertEquals(2, configuration.getGrpcServerOption().getFlowControlWindow());

        assertEquals(2, configuration.getGrpcServerOption().getHandshakeTimeout());
        assertEquals(2, configuration.getGrpcServerOption().getReceiveBufferSize());
    }
}