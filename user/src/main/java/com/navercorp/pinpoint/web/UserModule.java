package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.config.UserConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModule {
    @Bean
    public UserConfigProperties userConfigProperties() {
        return new UserConfigProperties();
    }
}
