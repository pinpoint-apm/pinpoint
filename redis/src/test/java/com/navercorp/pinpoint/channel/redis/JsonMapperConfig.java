package com.navercorp.pinpoint.channel.redis;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;

public class JsonMapperConfig {
    @Bean
    public JsonMapper jsonMapper() {
        return new JsonMapper();
    }
}
