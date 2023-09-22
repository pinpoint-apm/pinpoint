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

import com.navercorp.pinpoint.collector.receiver.BindAddress;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableConfigurationProperties
@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = {
        GrpcAgentDataReceiverConfiguration.class,
        TestReceiverConfig.class,
})
@ExtendWith(SpringExtension.class)
public class GrpcAgentDataReceiverConfigurationTest {

    @Autowired
    private GrpcAgentDataReceiverProperties configuration;

    @Autowired
    @Qualifier("grpcAgentServerExecutorProperties")
    MonitoringExecutorProperties serverExecutor;
    @Autowired
    @Qualifier("grpcAgentServerCallExecutorProperties")
    MonitoringExecutorProperties serverCallExecutor;
    @Autowired
    @Qualifier("grpcAgentWorkerExecutorProperties")
    MonitoringExecutorProperties workerExecutor;

    @Test
    public void properties() {

        assertEquals(Boolean.FALSE, configuration.isEnable());
        BindAddress bindAddress = configuration.getBindAddress();
        assertEquals("1.1.1.1", bindAddress.getIp());
        assertEquals(1, bindAddress.getPort());

        assertEquals(10, serverExecutor.getCorePoolSize());
        assertEquals(11, serverExecutor.getQueueCapacity());

        assertEquals(20, workerExecutor.getCorePoolSize());
        assertEquals(21, workerExecutor.getQueueCapacity());

        assertTrue(workerExecutor.isMonitorEnable());

    }

    @Test
    public void serverOption() {

        ServerOption serverOption = configuration.getServerOption();

        assertEquals(1, serverOption.getKeepAliveTime());
        assertEquals(1, serverOption.getKeepAliveTimeout());
        assertEquals(1, serverOption.getPermitKeepAliveTime());
        assertEquals(1, serverOption.getMaxConnectionIdle());
        assertEquals(1, serverOption.getMaxConcurrentCallsPerConnection());
        // 1M
        assertEquals(1024 * 1024, serverOption.getMaxInboundMessageSize());
        // 1K
        assertEquals(1024, serverOption.getMaxHeaderListSize());
        // 1M
        assertEquals(1024 * 1024, serverOption.getFlowControlWindow());

        assertEquals(1, serverOption.getHandshakeTimeout());
        // 1M
        assertEquals(1024 * 1024, serverOption.getReceiveBufferSize());
    }
}