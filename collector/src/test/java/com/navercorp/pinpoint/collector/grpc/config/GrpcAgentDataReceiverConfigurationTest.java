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
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableConfigurationProperties
@TestPropertySource(properties = {
        // # Agent
        "collector.receiver.grpc.agent.enable=false",
        "collector.receiver.grpc.agent.bindaddress.ip=1.1.1.1",
        "collector.receiver.grpc.agent.bindaddress.port=1",
        // # Executor of Server
        "collector.receiver.grpc.agent.server.executor.corePoolSize=1",
        "collector.receiver.grpc.agent.server.executor.maxPoolSize=1",
        "collector.receiver.grpc.agent.server.executor.queueCapacity=11",
        "collector.receiver.grpc.agent.server.executor.monitor-enable=true",
        // # Executor of Worker
        "collector.receiver.grpc.agent.worker.executor.corePoolSize=1",
        "collector.receiver.grpc.agent.worker.executor.maxPoolSize=1",
        "collector.receiver.grpc.agent.worker.executor.queueCapacity=21",
        "collector.receiver.grpc.agent.worker.executor.monitor-enable=true",
        // # Server Option
        "collector.receiver.grpc.agent.keepalive_time_millis=1",
        "collector.receiver.grpc.agent.keepalive_timeout_millis=2",
        "collector.receiver.grpc.agent.permit_keepalive_time_millis=3",
        "collector.receiver.grpc.agent.connection_idle_timeout_millis=4",
        "collector.receiver.grpc.agent.concurrent-calls_per-connection_max=1",
        "collector.receiver.grpc.agent.handshake_timeout_millis=1",
        "collector.receiver.grpc.agent.flow-control_window_size_init=1MB",
        "collector.receiver.grpc.agent.header_list_size_max=1KB",
        "collector.receiver.grpc.agent.inbound_message_size_max=1MB",
        "collector.receiver.grpc.agent.receive_buffer_size=1MB",
})
@ContextConfiguration(classes = {
        GrpcAgentDataReceiverConfiguration.class,
        TestReceiverConfig.class,
})
@ExtendWith(SpringExtension.class)
public class GrpcAgentDataReceiverConfigurationTest {

    @Autowired
    GrpcReceiverProperties properties;

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

        assertFalse(properties.isEnable());

        BindAddress bindAddress = properties.getBindAddress();
        assertEquals("1.1.1.1", bindAddress.getIp());
        assertEquals(1, bindAddress.getPort());

        assertEquals(1, serverExecutor.getCorePoolSize());
        assertEquals(1, serverExecutor.getMaxPoolSize());
        assertEquals(11, serverExecutor.getQueueCapacity());

        assertEquals(1, workerExecutor.getCorePoolSize());
        assertEquals(1, workerExecutor.getMaxPoolSize());
        assertEquals(21, workerExecutor.getQueueCapacity());

        assertTrue(workerExecutor.isMonitorEnable());

    }

    @Test
    public void serverOption() {

        ServerOption serverOption = properties.getServerOption();

        assertEquals(1, serverOption.getKeepAliveTime());
        assertEquals(2, serverOption.getKeepAliveTimeout());
        assertEquals(3, serverOption.getPermitKeepAliveTime());
        assertEquals(4, serverOption.getMaxConnectionIdle());
        assertEquals(1, serverOption.getMaxConcurrentCallsPerConnection());
        // 1M
        assertEquals(DataSize.ofMegabytes(1).toBytes(), serverOption.getMaxInboundMessageSize());
        // 1K
        assertEquals(DataSize.ofKilobytes(1).toBytes(), serverOption.getMaxHeaderListSize());
        // 1M
        assertEquals(DataSize.ofMegabytes(1).toBytes(), serverOption.getFlowControlWindow());

        assertEquals(1, serverOption.getHandshakeTimeout());
        // 1M
        assertEquals(DataSize.ofMegabytes(1).toBytes(), serverOption.getReceiveBufferSize());
    }
}