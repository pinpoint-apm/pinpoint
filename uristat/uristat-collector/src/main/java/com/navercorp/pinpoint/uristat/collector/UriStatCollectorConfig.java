package com.navercorp.pinpoint.uristat.collector;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("metric")
@ComponentScan({"com.navercorp.pinpoint.uristat.collector.service", "com.navercorp.pinpoint.uristat.collector.dao"})
@PropertySource({"classpath:kafka-topic.properties", "classpath:kafka-producer-factory.properties"})
@ImportResource({"classpath*:**/applicationContext-collector-metric-namespace.xml", "classpath:applicationContext-collector-pinot-kafka.xml"})
public class UriStatCollectorConfig {
}
