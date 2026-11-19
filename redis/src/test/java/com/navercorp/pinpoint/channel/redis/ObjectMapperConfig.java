package com.navercorp.pinpoint.channel.redis;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;

public class ObjectMapperConfig {
    @Bean
    public JsonMapper objectMapper() {
        return new JsonMapper();
    }
}
