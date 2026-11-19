package com.navercorp.pinpoint.metric.common.config;

import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class MetricCommonConfiguration {

    @Bean
    public CommonRegistryHandler commonRegistryHandler(JsonMapper jsonMapper) {
        return new CommonRegistryHandler(jsonMapper);
    }
}
