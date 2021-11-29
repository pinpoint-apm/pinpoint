package com.navercorp.pinpoint.metric.collector;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class, KafkaAutoConfiguration.class})
@ImportResource({"classpath:pinot-collector/applicationContext-collector-pinot.xml", "classpath:pinot-collector/servlet-context-collector-pinot.xml"})
@Import({MetricAppPropertySources.class})
@Profile("metric")
public class MetricCollectorApp {
}
