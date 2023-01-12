package com.navercorp.pinpoint.uristat.collector;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("metric")
@Configuration
@ImportResource({"classpath*:**/applicationContext-collector-metric-namespace.xml", "classpath:applicationContext-collector-uristat-pinot-kafka.xml"})
@ComponentScan({"com.navercorp.pinpoint.uristat.collector.service", "com.navercorp.pinpoint.uristat.collector.dao"})
@PropertySource({UriStatCollectorConfig.KAFKA_TOPIC_PROPERTIES, UriStatCollectorConfig.KAFKA_PRODUCER_FACTORY_PROPERTIES})
public class UriStatCollectorConfig {
    public static final String KAFKA_TOPIC_PROPERTIES = "classpath:profiles/${pinpoint.profiles.active}/kafka-topic.properties";

    public static final String KAFKA_PRODUCER_FACTORY_PROPERTIES = "classpath:profiles/${pinpoint.profiles.active}/kafka-producer-factory.properties";
}
