package com.navercorp.pinpoint.metric.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.collector.cache.MetricCacheConfiguration;
import com.navercorp.pinpoint.metric.collector.config.MetricKafkaConfiguration;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;


@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class, KafkaAutoConfiguration.class})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.metric.collector.dao",
        "com.navercorp.pinpoint.metric.collector.service",
        "com.navercorp.pinpoint.metric.common.model",
        "com.navercorp.pinpoint.metric.common",
        "com.navercorp.pinpoint.common.server.util",
        "com.navercorp.pinpoint.metric.collector.controller"
})
@ImportResource({
        "classpath:pinot-collector/applicationContext-collector-pinot-dao-config.xml"
})
@Import({
        MetricAppPropertySources.class,
        MetricCacheConfiguration.class,
        PinotConfiguration.class,
        MetricKafkaConfiguration.class
})
@Profile("metric")
public class MetricCollectorApp {

    @Bean("objectMapper")
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }




}
