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

@EnableConfigurationProperties
@TestPropertySource(properties = {
        // # Stat
        "collector.receiver.grpc.stat.enable=false",
        "collector.receiver.grpc.stat.bindaddress.ip=2.2.2.2",
        "collector.receiver.grpc.stat.bindaddress.port=2",

        // # Executor of Worker
        "collector.receiver.grpc.stat.worker.executor.corePoolSize=2",
        "collector.receiver.grpc.stat.worker.executor.maxPoolSize=2",
        "collector.receiver.grpc.stat.worker.executor.queueCapacity=2",
        "collector.receiver.grpc.stat.worker.executor.monitor-enable=false",

        // # Stream scheduler for rejected execution
        "collector.receiver.grpc.stat.stream.scheduler_thread_size=2",
        "collector.receiver.grpc.stat.stream.scheduler_period_millis=2",
        "collector.receiver.grpc.stat.stream.call_init_request_count=2",
        "collector.receiver.grpc.stat.stream.throttled_logger_ratio=2",

        // # Server Option
        "collector.receiver.grpc.stat.keepalive_time_millis=2",
        "collector.receiver.grpc.stat.keepalive_timeout_millis=3",
        "collector.receiver.grpc.stat.permit_keepalive_time_millis=4",
        "collector.receiver.grpc.stat.connection_idle_timeout_millis=5",
        "collector.receiver.grpc.stat.concurrent-calls_per-connection_max=6",
        "collector.receiver.grpc.stat.handshake_timeout_millis=2",
        "collector.receiver.grpc.stat.flow-control_window_size_init=2MB",
        "collector.receiver.grpc.stat.header_list_size_max=2KB",
        "collector.receiver.grpc.stat.inbound_message_size_max=2MB",
        "collector.receiver.grpc.stat.receive_buffer_size=2MB",
})
@ContextConfiguration(classes = {
        GrpcStatReceiverConfiguration.class,
        TestReceiverConfig.class
})
@ExtendWith(SpringExtension.class)
public class GrpcStatReceiverConfigurationTest {

    @Autowired
    GrpcReceiverProperties properties;
    @Autowired
    GrpcStreamProperties streamProperties;

    @Autowired
    @Qualifier("grpcStatWorkerExecutorProperties")
    MonitoringExecutorProperties workerExecutor;

    @Test
    public void properties() {

        assertFalse(properties.isEnable());

        BindAddress bindAddress = properties.getBindAddress();
        assertEquals("2.2.2.2", bindAddress.getIp());
        assertEquals(2, bindAddress.getPort());

        assertEquals(2, workerExecutor.getCorePoolSize());
        assertEquals(2, workerExecutor.getQueueCapacity());
        assertFalse(workerExecutor.isMonitorEnable());

        assertEquals(2, streamProperties.getSchedulerThreadSize());
        assertEquals(2, streamProperties.getSchedulerPeriodMillis());
        assertEquals(2, streamProperties.getCallInitRequestCount());
        assertEquals(2, streamProperties.getThrottledLoggerRatio());

    }


    @Test
    public void serverOption() {

        ServerOption serverOption = properties.getServerOption();

        assertEquals(2, serverOption.getKeepAliveTime());
        assertEquals(3, serverOption.getKeepAliveTimeout());
        assertEquals(4, serverOption.getPermitKeepAliveTime());
        assertEquals(5, serverOption.getMaxConnectionIdle());
        assertEquals(6, serverOption.getMaxConcurrentCallsPerConnection());
        // 2M
        assertEquals(DataSize.ofMegabytes(2).toBytes(), serverOption.getMaxInboundMessageSize());
        // 2K
        assertEquals(DataSize.ofKilobytes(2).toBytes(), serverOption.getMaxHeaderListSize());
        // 2M
        assertEquals(DataSize.ofMegabytes(2).toBytes(), serverOption.getFlowControlWindow());

        assertEquals(2, serverOption.getHandshakeTimeout());
        // 2M
        assertEquals(DataSize.ofMegabytes(2).toBytes(), serverOption.getReceiveBufferSize());
    }

}