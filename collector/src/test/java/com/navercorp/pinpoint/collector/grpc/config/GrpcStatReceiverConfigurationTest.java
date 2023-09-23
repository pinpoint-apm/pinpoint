/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnableConfigurationProperties
@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = {
        GrpcStatReceiverConfiguration.class,
        TestReceiverConfig.class
})
@ExtendWith(SpringExtension.class)
public class GrpcStatReceiverConfigurationTest {

    @Autowired
    private GrpcStatReceiverProperties configuration;

    @Autowired
    @Qualifier("grpcStatWorkerExecutorProperties")
    MonitoringExecutorProperties workerExecutor;

    @Test
    public void properties() {

        assertFalse(configuration.isEnable());
        assertEquals("2.2.2.2", configuration.getBindAddress().getIp());
        assertEquals(2, configuration.getBindAddress().getPort());
        assertEquals(2, workerExecutor.getCorePoolSize());
        assertEquals(2, workerExecutor.getQueueCapacity());
        assertFalse(workerExecutor.isMonitorEnable());

        GrpcStreamProperties streamProperties = configuration.getStreamProperties();
        assertEquals(2, streamProperties.getSchedulerThreadSize());
        assertEquals(2, streamProperties.getSchedulerPeriodMillis());
        assertEquals(2, streamProperties.getCallInitRequestCount());
        assertEquals(2, streamProperties.getThrottledLoggerRatio());

    }


    @Test
    public void serverOption() {

        ServerOption serverOption = configuration.getServerOption();

        assertEquals(2, serverOption.getKeepAliveTime());
        assertEquals(2, serverOption.getKeepAliveTimeout());
        assertEquals(2, serverOption.getPermitKeepAliveTime());
        assertEquals(2, serverOption.getMaxConnectionIdle());
        assertEquals(2, serverOption.getMaxConcurrentCallsPerConnection());
        // 2M
        assertEquals(2 * 1024 * 1024, serverOption.getMaxInboundMessageSize());
        // 2K
        assertEquals(2 * 1024, serverOption.getMaxHeaderListSize());
        // 2M
        assertEquals(2 * 1024 * 1024, serverOption.getFlowControlWindow());

        assertEquals(2, serverOption.getHandshakeTimeout());
        // 2M
        assertEquals(2 * 1024 * 1024, serverOption.getReceiveBufferSize());
    }

}