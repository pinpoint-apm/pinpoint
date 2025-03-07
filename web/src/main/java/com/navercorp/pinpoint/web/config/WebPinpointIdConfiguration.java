package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomServiceUidGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        WebApplicationIdCacheConfig.class,
        WebV4CacheConfig.class,
})
public class WebPinpointIdConfiguration {

    private final Logger logger = LogManager.getLogger(WebPinpointIdConfiguration.class);

    public WebPinpointIdConfiguration() {
        logger.info("Install {}", WebPinpointIdConfiguration.class.getSimpleName());
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
    public IdGenerator<ServiceUid> serviceUidGenerator() {
        return new RandomServiceUidGenerator();
    }
}
