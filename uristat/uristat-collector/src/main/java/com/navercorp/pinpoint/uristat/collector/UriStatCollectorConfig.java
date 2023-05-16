package com.navercorp.pinpoint.uristat.collector;


import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import com.navercorp.pinpoint.uristat.collector.config.UriMetricKafkaConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("uri")
@Configuration
@Import({PinotConfiguration.class, UriMetricKafkaConfiguration.class})
@ComponentScan({"com.navercorp.pinpoint.uristat.collector.service", "com.navercorp.pinpoint.uristat.collector.dao"})
@PropertySource({UriStatCollectorConfig.KAFKA_TOPIC_PROPERTIES})
public class UriStatCollectorConfig {
    public static final String KAFKA_TOPIC_PROPERTIES = "classpath:profiles/${pinpoint.profiles.active}/kafka-topic.properties";


}
