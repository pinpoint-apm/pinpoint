package com.navercorp.pinpoint.web.frontend;

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.frontend.config.ExperimentalProperties;
import com.navercorp.pinpoint.web.frontend.export.ConfigPropertiesExporter;
import com.navercorp.pinpoint.web.frontend.export.ExperimentalPropertiesExporter;
import com.navercorp.pinpoint.web.frontend.export.FrontendConfigExporter;
import com.navercorp.pinpoint.web.frontend.export.UserServiceConfigExporter;
import com.navercorp.pinpoint.web.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Configuration
public class ExportConfiguration {

    @Bean
    public ExperimentalProperties experimentalProperties(Environment env) {
        return ExperimentalProperties.of(env);
    }

    @Bean
    public FrontendConfigExporter configPropertiesExporter(Optional<ConfigProperties> properties) {
        if (properties.isEmpty()) {
            return null;
        }
        return new ConfigPropertiesExporter(properties.get());
    }

    @Bean
    public FrontendConfigExporter experimentalPropertiesExporter(Optional<ExperimentalProperties> properties) {
        if (properties.isEmpty()) {
            return null;
        }
        return new ExperimentalPropertiesExporter(properties.get());
    }

    @Bean
    public FrontendConfigExporter userServiceConfigExporter(Optional<UserService> service) {
        if (service.isEmpty()) {
            return null;
        }
        return new UserServiceConfigExporter(service.get());
    }
}
