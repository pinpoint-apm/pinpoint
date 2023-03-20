package com.navercorp.pinpoint.pinot.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@PropertySource("classpath:/profiles/${pinpoint.profiles.active:release}/kafka-producer-factory.properties")
public class KafkaConfiguration {

    private final Logger logger = LogManager.getLogger(KafkaConfiguration.class);

    public Map<String, Object> toConfig(KafkaProperties properties) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, properties.getKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, properties.getValueSerializer());

        config.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, properties.getPartitionerClass());
        config.put(ProducerConfig.ACKS_CONFIG, properties.getAcks());
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, properties.getCompressionType());

        return config;
    }

    @Bean
    public ProducerFactory kafkaProducerFactory(KafkaProperties properties) {
        logger.info("kafka {}:{}", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        logger.debug("kafka config:{}", properties);

        Map<String, Object> config = toConfig(properties);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaProperties kafkaProperties(Environment env) {
        KafkaProperties properties = new KafkaProperties();

        bindProperties(env, "pinpoint.metric.kafka.bootstrap.servers", properties::setBootstrapServers);
        bindProperties(env, "pinpoint.metric.kafka.key.serializer", properties::setKeySerializer);
        bindProperties(env, "pinpoint.metric.kafka.value.serializer", properties::setValueSerializer);
        bindProperties(env, "pinpoint.metric.kafka.acks", properties::setAcks);
        bindProperties(env, "pinpoint.metric.kafka.compressionType", properties::setCompressionType);

        return properties;
    }

    private void bindProperties(Environment env, String key, Consumer<String> consumer) {
        String value = env.getProperty(key);
        if (value != null) {
            consumer.accept(value);
        }
    }

}
