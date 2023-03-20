package com.navercorp.pinpoint.metric.collector.config;

import com.navercorp.pinpoint.metric.collector.dao.pinot.PinotMetricTagDao;
import com.navercorp.pinpoint.metric.collector.view.SystemMetricView;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.pinot.kafka.KafkaConfiguration;
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
public class MetricKafkaConfiguration {

    @Bean
    public KafkaTemplate<String, SystemMetricView> kafkaDoubleTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, PinotMetricTagDao.MetricJsonTag> kafkaTagTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, MetricData> kafkaDataTypeTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
