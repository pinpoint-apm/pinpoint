package com.navercorp.pinpoint.uristat.collector.config;

import com.navercorp.pinpoint.pinot.kafka.KafkaConfiguration;
import com.navercorp.pinpoint.uristat.collector.model.UriStat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
@Import({KafkaConfiguration.class})
public class UriMetricKafkaConfiguration {

    @Bean
    public KafkaTemplate<String, UriStat> kafkaUriStatTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
