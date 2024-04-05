package com.navercorp.pinpoint.pinot.kafka;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@PropertySource(value = {
        "classpath:/kafka-root.properties",
        "classpath:/profiles/${pinpoint.profiles.active:release}/kafka-producer-factory.properties"
})
@EnableConfigurationProperties
public class KafkaConfiguration {

    private final Logger logger = LogManager.getLogger(KafkaConfiguration.class);

    public KafkaConfiguration() {
        logger.info("Install {}", KafkaConfiguration.class);
    }

    @Bean
    public ProducerFactory<?, ?> kafkaProducerFactory(
            ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {

        KafkaProperties properties = kafkaProperties();
        // ref : KafkaAutoConfiguration
        Map<String, Object> producerProperties = properties.buildProducerProperties();
        logger.info("ProducerProperties:{}", producerProperties);

        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(producerProperties);

        KafkaProperties.Producer producer = properties.getProducer();
        String transactionIdPrefix = producer.getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }

        customizers.orderedStream().forEach((customizer) -> customizer.customize(factory));
        return factory;
    }

    @Bean
    @ConfigurationProperties(prefix = "pinpoint.metric.kafka")
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }


}
