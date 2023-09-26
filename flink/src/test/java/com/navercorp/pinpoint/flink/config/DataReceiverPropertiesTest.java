package com.navercorp.pinpoint.flink.config;

import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.annotation.Validated;


@TestPropertySource(properties = {
        "flink.receiver.base.ip:0.0.0.2",
        "flink.receiver.base.port:39994",

        "flink.receiver.base.worker.corePoolSize:33",
        "flink.receiver.base.worker.maxPoolSize:32",
        "flink.receiver.base.worker.queueCapacity:29",
        "flink.receiver.base.worker.monitor-enable:true",

})
@ContextConfiguration(classes = {
        DataReceiverProperties.class,
        DataReceiverPropertiesTest.DataReceiverTestConfig.class
})
@ExtendWith(SpringExtension.class)
class DataReceiverPropertiesTest {

    @Configuration
    @EnableConfigurationProperties
    static class DataReceiverTestConfig {
        @Bean
        @Validated
        @ConfigurationProperties("flink.receiver.base.worker")
        public MonitoringExecutorProperties flinkWorkerExecutorProperties() {
            return new MonitoringExecutorProperties();
        }
    }

    @Autowired
    DataReceiverProperties properties;
    @Autowired
    MonitoringExecutorProperties executorProperties;

    @Test
    public void properties() {
        Assertions.assertEquals("0.0.0.2", properties.getBindIp());
        Assertions.assertEquals(39994, properties.getBindPort());
        Assertions.assertEquals(33, executorProperties.getCorePoolSize());
        Assertions.assertEquals(32, executorProperties.getMaxPoolSize());
        Assertions.assertEquals(29, executorProperties.getQueueCapacity());
        Assertions.assertTrue(executorProperties.isMonitorEnable());
    }

}