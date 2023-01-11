package com.navercorp.pinpoint.uristat.collector;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("metric")
@ComponentScan({"com.navercorp.pinpoint.uristat.collector.service", "com.navercorp.pinpoint.uristat.collector.dao"})
@PropertySource({UriStatCollectorConfig.KAFKA_TOPIC_PROPERTIES, UriStatCollectorConfig.KAFKA_PRODUCER_FACTORY_PROPERTIES})
@ImportResource({"classpath*:**/applicationContext-collector-metric-namespace.xml", "classpath:applicationContext-collector-pinot-kafka.xml"})
public class UriStatCollectorConfig {
    public static final String KAFKA_TOPIC_PROPERTIES = "classpath:profiles/${pinpoint.profiles.active}/kafka-topic.properties";

    public static final String KAFKA_PRODUCER_FACTORY_PROPERTIES = "classpath:profiles/${pinpoint.profiles.active}/kafka-producer-factory.properties";
}
