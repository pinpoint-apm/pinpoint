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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = GrpcStatReceiverConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class GrpcStatReceiverConfigurationTest {

    @Autowired
    GrpcStatReceiverConfiguration configuration;

    @Test
    public void properties() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-pinpoint-collector.properties");
        configuration.loadServerOption(properties);

        assertEquals(Boolean.FALSE, configuration.isGrpcEnable());
        assertEquals("2.2.2.2", configuration.getGrpcBindIp());
        assertEquals(2, configuration.getGrpcBindPort());
        assertEquals(2, configuration.getGrpcWorkerExecutorThreadSize());
        assertEquals(2, configuration.getGrpcWorkerExecutorQueueSize());
        assertEquals(Boolean.FALSE, configuration.isGrpcWorkerExecutorMonitorEnable());
        assertEquals(2, configuration.getGrpcStreamSchedulerThreadSize());
        assertEquals(2, configuration.getGrpcStreamSchedulerPeriodMillis());
        assertEquals(2, configuration.getGrpcStreamCallInitRequestCount());

        assertEquals(2, configuration.getGrpcServerOption().getKeepAliveTime());
        assertEquals(2, configuration.getGrpcServerOption().getKeepAliveTimeout());
        assertEquals(2, configuration.getGrpcServerOption().getPermitKeepAliveTime());
        assertEquals(2, configuration.getGrpcServerOption().getMaxConnectionIdle());
        assertEquals(2, configuration.getGrpcServerOption().getMaxConcurrentCallsPerConnection());
        // 2M
        assertEquals(2 * 1024 * 1024, configuration.getGrpcServerOption().getMaxInboundMessageSize());
        // 2K
        assertEquals(2 * 1024, configuration.getGrpcServerOption().getMaxHeaderListSize());
        // 2M
        assertEquals(2 * 1024 * 1024, configuration.getGrpcServerOption().getFlowControlWindow());

        assertEquals(2, configuration.getGrpcServerOption().getHandshakeTimeout());
        // 2M
        assertEquals(2 * 1024 * 1024, configuration.getGrpcServerOption().getReceiveBufferSize());
    }

}