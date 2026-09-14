package com.navercorp.pinpoint.pinot.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = KafkaConfiguration.class )
@TestPropertySource(properties = {"pinpoint.metric.kafka.bootstrap.servers=localhost:19092", "pinpoint.metric.kafka.max.block.ms=3000", "pinpoint.metric.kafka.buffer.memory=67108864", "pinpoint.metric.kafka.linger.ms=5",
        // JsonSerializer needs jackson-databind, which is not on this module's test classpath
        "pinpoint.metric.kafka.value.serializer=org.apache.kafka.common.serialization.StringSerializer"})
class KafkaConfigurationTest {
    @Autowired
    @Qualifier("kafkaProducerFactory")
    ProducerFactory producerFactory;

    @Test
    void test() {
        Assertions.assertEquals("localhost:19092", producerFactory.getConfigurationProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        Assertions.assertEquals(3000L, producerFactory.getConfigurationProperties().get(ProducerConfig.MAX_BLOCK_MS_CONFIG));
        Assertions.assertEquals(67108864L, producerFactory.getConfigurationProperties().get(ProducerConfig.BUFFER_MEMORY_CONFIG));
        Assertions.assertEquals(5L, producerFactory.getConfigurationProperties().get(ProducerConfig.LINGER_MS_CONFIG));
    }

    @Test
    void bufferMemory_appliedToProducer() {
        // KafkaProducer does not connect on construction, only the bootstrap address format is validated
        try (Producer<?, ?> producer = producerFactory.createProducer()) {
            Assertions.assertEquals(67108864.0, bufferTotalBytes(producer));
        }
    }

    private static double bufferTotalBytes(Producer<?, ?> producer) {
        for (Map.Entry<MetricName, ? extends Metric> entry : producer.metrics().entrySet()) {
            MetricName name = entry.getKey();
            if ("producer-metrics".equals(name.group()) && "buffer-total-bytes".equals(name.name())) {
                return (Double) entry.getValue().metricValue();
            }
        }
        throw new AssertionError("buffer-total-bytes metric not found");
    }

    @Test
    void optionalProducerConfig_default_notSet() {
        KafkaConfiguration configuration = new KafkaConfiguration();
        Map<String, Object> config = configuration.toConfig(new KafkaProperties());
        Assertions.assertFalse(config.containsKey(ProducerConfig.BUFFER_MEMORY_CONFIG));
        Assertions.assertFalse(config.containsKey(ProducerConfig.LINGER_MS_CONFIG));
    }
}