package com.navercorp.pinpoint.flink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource(name = "FlinkModule", value = {
        "classpath:profiles/${pinpoint.profiles.active:local}/hbase.properties",
        "classpath:profiles/${pinpoint.profiles.active:local}/pinpoint-flink.properties"
})
public class PropertySourcesPlaceholderConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer flinkPropertyConfigurerLocal() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
