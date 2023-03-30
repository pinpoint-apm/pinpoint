package com.navercorp.pinpoint.web.webhook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.web.webhook.controller",
        "com.navercorp.pinpoint.web.webhook.service",
        "com.navercorp.pinpoint.web.webhook.dao",
})
public class WebhookModule {
    private final Logger logger = LogManager.getLogger(WebhookModule.class);

    public WebhookModule() {
        logger.info("Install {}", WebhookModule.class.getSimpleName());
    }
}
