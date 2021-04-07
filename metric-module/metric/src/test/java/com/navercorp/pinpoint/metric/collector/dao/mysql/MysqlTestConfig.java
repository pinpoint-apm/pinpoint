package com.navercorp.pinpoint.metric.collector.dao.mysql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@ImportResource({"classpath:pinot-collector/applicationContext-collector-pinot.xml"})
@PropertySource({"classpath:/pinot-collector/profiles/${pinpoint.profiles.active}/jdbc.properties", "classpath:/pinot-collector/kafka-producer-factory.properties", "classpath:/pinot-collector/kafka-topic.properties"})
@Configuration()
public class MysqlTestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
