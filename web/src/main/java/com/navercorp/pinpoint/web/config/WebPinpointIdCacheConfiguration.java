package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.common.server.config.PinpointIdCacheConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        PinpointIdCacheConfiguration.class,
})
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class WebPinpointIdCacheConfiguration {

    private final Logger logger = LogManager.getLogger(WebPinpointIdCacheConfiguration.class);

    public WebPinpointIdCacheConfiguration() {
        logger.info("Install {}", WebPinpointIdCacheConfiguration.class.getSimpleName());
    }
}
