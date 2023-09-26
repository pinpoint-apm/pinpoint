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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * @author Woonduk Kang(emeroad)
 */
@EnableConfigurationProperties
@TestPropertySource(properties = {
        // # Stat
        "collector.receiver.grpc.span.enable=false",
        "collector.receiver.grpc.span.bindaddress.ip=2.2.2.2",
        "collector.receiver.grpc.span.bindaddress.port=2",

        // # Executor of Worker
        "collector.receiver.grpc.span.worker.executor.corePoolSize=1",
        "collector.receiver.grpc.span.worker.executor.maxPoolSize=1",
        "collector.receiver.grpc.span.worker.executor.queueCapacity=4",
        "collector.receiver.grpc.span.worker.executor.monitor-enable=false",

        // # Stream scheduler for rejected execution
        "collector.receiver.grpc.span.stream.scheduler_thread_size=2",
        "collector.receiver.grpc.span.stream.scheduler_period_millis=3",
        "collector.receiver.grpc.span.stream.call_init_request_count=4",
        "collector.receiver.grpc.span.stream.throttled_logger_ratio=5",

        // # Server Option
        "collector.receiver.grpc.span.keepalive_time_millis=2",
        "collector.receiver.grpc.span.keepalive_timeout_millis=3",
        "collector.receiver.grpc.span.permit_keepalive_time_millis=4",
        "collector.receiver.grpc.span.connection_idle_timeout_millis=5",
        "collector.receiver.grpc.span.concurrent-calls_per-connection_max=6",
        "collector.receiver.grpc.span.handshake_timeout_millis=2",
        "collector.receiver.grpc.span.flow-control_window_size_init=3MB",
        "collector.receiver.grpc.span.header_list_size_max=3KB",
        "collector.receiver.grpc.span.inbound_message_size_max=3MB",
        "collector.receiver.grpc.span.receive_buffer_size=3MB",
})
@ContextConfiguration(classes = {
        GrpcSpanReceiverConfiguration.class,
        TestReceiverConfig.class,
})
@ExtendWith(SpringExtension.class)
public class GrpcSpanReceiverConfigurationTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    GrpcReceiverProperties properties;
    @Autowired
    GrpcStreamProperties streamProperties;

    @Autowired
    @Qualifier("grpcSpanWorkerExecutorProperties")
    MonitoringExecutorProperties workerExecutor;

    @Test
    public void properties() {

        assertFalse(properties.isEnable());

        BindAddress bindAddress = properties.getBindAddress();
        assertEquals("2.2.2.2", bindAddress.getIp());
        assertEquals(2, bindAddress.getPort());

        assertEquals(1, workerExecutor.getCorePoolSize());
        assertEquals(1, workerExecutor.getMaxPoolSize());
        assertEquals(4, workerExecutor.getQueueCapacity());
        assertFalse(workerExecutor.isMonitorEnable());

        assertEquals(2, streamProperties.getSchedulerThreadSize());
        assertEquals(3, streamProperties.getSchedulerPeriodMillis());
        assertEquals(4, streamProperties.getCallInitRequestCount());
        assertEquals(5, streamProperties.getThrottledLoggerRatio());
    }

    @Test
    public void serverOption() {

        ServerOption serverOption = properties.getServerOption();

        assertEquals(2, serverOption.getKeepAliveTime());
        assertEquals(3, serverOption.getKeepAliveTimeout());
        assertEquals(4, serverOption.getPermitKeepAliveTime());
        assertEquals(5, serverOption.getMaxConnectionIdle());
        assertEquals(6, serverOption.getMaxConcurrentCallsPerConnection());
        // 3M
        assertEquals(DataSize.ofMegabytes(3).toBytes(), serverOption.getMaxInboundMessageSize());
        // 3K
        assertEquals(DataSize.ofKilobytes(3).toBytes(), serverOption.getMaxHeaderListSize());
        // 3M
        assertEquals(DataSize.ofMegabytes(3).toBytes(), serverOption.getFlowControlWindow());

        assertEquals(2, serverOption.getHandshakeTimeout());
        // 3M
        assertEquals(DataSize.ofMegabytes(3).toBytes(), serverOption.getReceiveBufferSize());
    }

}