package com.navercorp.pinpoint.web.frontend;

import com.navercorp.pinpoint.web.frontend.config.ExperimentalProperties;
import com.navercorp.pinpoint.web.frontend.controller.FrontendConfigController;
import com.navercorp.pinpoint.web.frontend.export.FrontendConfigExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@Import(ExportConfiguration.class)
public class FrontendConfigExportConfiguration {
    private final Logger logger = LogManager.getLogger(FrontendConfigExportConfiguration.class);

    public FrontendConfigExportConfiguration() {
        logger.info("Install {}", FrontendConfigExportConfiguration.class.getSimpleName());
    }

    @Bean
    public FrontendConfigController configController(List<FrontendConfigExporter> exporters) {
        return new FrontendConfigController(exporters);
    }

    @Bean
    public ExperimentalProperties experimentalConfig(Environment env) {
        return new ExperimentalProperties(env);
    }

}
