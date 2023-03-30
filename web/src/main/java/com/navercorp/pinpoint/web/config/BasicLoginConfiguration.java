package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.web.security.login.BasicLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("basicLogin")
public class BasicLoginConfiguration {
    @Bean
    public BasicLoginConfig basicLoginConfig() {
        return new BasicLoginConfig();
    }

    @Bean
    public BasicLoginService basicLoginService(BasicLoginConfig basicLoginConfig) {
        return new BasicLoginService(basicLoginConfig);
    }

}
