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

    @Bean("kafkaDoubleTemplate")
    public KafkaTemplate<String, SystemMetricView> getSystemMetricKafkaTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean("kafkaTagTemplate")
    public KafkaTemplate<String, PinotMetricTagDao.MetricJsonTag> getTagKafkaTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean("kafkaDataTypeTemplate")
    public KafkaTemplate<String, MetricData> getDataTypeKafkaTemplate(@Qualifier("kafkaProducerFactory") ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
