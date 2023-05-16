package com.navercorp.pinpoint.web.query;

import com.navercorp.pinpoint.web.query.service.QueryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.query.controller",
        "com.navercorp.pinpoint.web.query.service"
})
public class QueryServiceConfiguration {
    private final Logger logger = LogManager.getLogger(QueryServiceConfiguration.class);

    public QueryServiceConfiguration() {
        logger.info("Install {}", QueryService.class.getSimpleName());
    }
}
