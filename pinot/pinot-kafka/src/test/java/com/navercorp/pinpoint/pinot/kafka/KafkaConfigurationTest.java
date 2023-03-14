package com.navercorp.pinpoint.pinot.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ProducerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
@SpringBootTest(classes = KafkaConfiguration.class,
        properties = {"pinpoint.metric.kafka.bootstrap.servers=test-kafka-bootstrap"})
class KafkaConfigurationTest {
    @Autowired
    ProducerFactory producerFactory;

    @Test
    void test() {
        Assertions.assertEquals("test-kafka-bootstrap", producerFactory.getConfigurationProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }
}