package com.navercorp.pinpoint.web.webhook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.web.webhook.controller",
        "com.navercorp.pinpoint.web.webhook.service",
        "com.navercorp.pinpoint.web.webhook.dao",
})
@ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "true", matchIfMissing = true)
public class WebhookModule {

    public static final String NAME = "pinpoint.modules.web.webhook";

    private final Logger logger = LogManager.getLogger(WebhookModule.class);

    public WebhookModule() {
        logger.info("Install {} (pinpoint.modules.web.webhook=true)", WebhookModule.class.getSimpleName());
    }
}
