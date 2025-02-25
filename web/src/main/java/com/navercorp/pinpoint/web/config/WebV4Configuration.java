package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomServiceUidGenerator;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        WebPinpointIdCacheConfig.class,
})
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class WebV4Configuration {

    private final Logger logger = LogManager.getLogger(WebV4Configuration.class);

    public WebV4Configuration() {
        logger.info("Install {}", WebV4Configuration.class.getSimpleName());
    }

    @Bean
    public IdGenerator<ServiceUid> serviceUidGenerator() {
        return new RandomServiceUidGenerator();
    }
}
