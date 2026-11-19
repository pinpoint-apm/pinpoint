package com.navercorp.pinpoint.metric.common.config;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricCommonConfiguration {

    @Bean
    public CommonRegistryHandler commonRegistryHandler(JsonMapper jsonMapper) {
        return new CommonRegistryHandler(jsonMapper);
    }
}
