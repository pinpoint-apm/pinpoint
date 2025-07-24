package com.navercorp.pinpoint.pinot.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Woonduk Kang(emeroad)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = KafkaConfiguration.class )
@TestPropertySource(properties = {"pinpoint.metric.kafka.bootstrap.servers=test-kafka-bootstrap", "pinpoint.metric.kafka.max.block.ms=3000"})
class KafkaConfigurationTest {
    @Autowired
    @Qualifier("kafkaProducerFactory")
    ProducerFactory producerFactory;

    @Test
    void test() {
        Assertions.assertEquals("test-kafka-bootstrap", producerFactory.getConfigurationProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        Object value = producerFactory.getConfigurationProperties().get(ProducerConfig.MAX_BLOCK_MS_CONFIG);
        Assertions.assertEquals(3000L, producerFactory.getConfigurationProperties().get(ProducerConfig.MAX_BLOCK_MS_CONFIG));
    }
}