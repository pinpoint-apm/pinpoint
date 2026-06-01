package com.pinpoint.test.springboot4.config;

import com.pinpoint.test.springboot4.controller.LegacyController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class UrlMappingConfig {

    @Bean
    public LegacyController legacyController() {
        return new LegacyController();
    }

    @Bean
    public SimpleUrlHandlerMapping legacyUrlHandlerMapping(LegacyController legacyController) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(0);
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("/legacy/*", legacyController);
        mapping.setUrlMap(urlMap);
        return mapping;
    }
}
