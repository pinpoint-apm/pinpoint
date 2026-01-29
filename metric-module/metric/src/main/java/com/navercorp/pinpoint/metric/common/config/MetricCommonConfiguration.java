package com.navercorp.pinpoint.metric.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricCommonConfiguration {

    @Bean
    public CommonRegistryHandler commonRegistryHandler(ObjectMapper objectMapper) {
        return new CommonRegistryHandler(objectMapper);
    }
}
