package com.navercorp.pinpoint.login.basic.config;

import com.navercorp.pinpoint.common.server.config.ActuatorProperties;
import com.navercorp.pinpoint.login.basic.service.BasicLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BasicLoginConfiguration {
    @Bean
    public BasicLoginProperties basicLoginConfig() {
        return new BasicLoginProperties();
    }

    @Bean
    public ActuatorProperties actuatorProperties() {
        return new ActuatorProperties();
    }

    @Bean
    public BasicLoginService basicLoginService(BasicLoginProperties basicLoginProperties) {
        return new BasicLoginService(basicLoginProperties);
    }

}
