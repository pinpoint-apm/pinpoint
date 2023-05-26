package com.navercorp.pinpoint.flink.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@TestPropertySource(properties = {
        "flink.receiver.base.ip:0.0.0.2",
        "flink.receiver.base.port:39994",

        "flink.receiver.base.worker.threadSize:33",
        "flink.receiver.base.worker.queueSize:29",
        "flink.receiver.base.worker.monitor:true",

})
@ContextConfiguration(classes = DataReceiverProperties.class)
@ExtendWith(SpringExtension.class)
class DataReceiverPropertiesTest {

    @Autowired
    DataReceiverProperties properties;

    @Test
    public void properties() {
        Assertions.assertEquals(properties.getBindIp(), "0.0.0.2");
        Assertions.assertEquals(properties.getBindPort(), 39994);
        Assertions.assertEquals(properties.getWorkerThreadSize(), 33);
        Assertions.assertEquals(properties.getWorkerQueueSize(), 29);
        Assertions.assertTrue(properties.isWorkerMonitorEnable());
    }

}