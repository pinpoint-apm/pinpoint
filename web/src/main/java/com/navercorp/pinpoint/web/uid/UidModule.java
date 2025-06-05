package com.navercorp.pinpoint.web.uid;

import com.navercorp.pinpoint.uid.UidCommonConfiguration;
import com.navercorp.pinpoint.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.config.WebApplicationUidConfig;
import com.navercorp.pinpoint.web.uid.config.WebServiceUidConfig;
import com.navercorp.pinpoint.web.uid.service.CachedApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.CachedApplicationUidServiceImpl;
import com.navercorp.pinpoint.web.uid.service.EmptyApplicationUidService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        UidCommonConfiguration.class,

        WebApplicationUidConfig.class,
        WebServiceUidConfig.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.web.uid.controller",
})
public class UidModule {

    private static final Logger logger = LogManager.getLogger(UidModule.class);

    public UidModule() {
        logger.info("Install UidModule");
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
    public CachedApplicationUidService cachedApplicationUidService(ApplicationUidService applicationUidService) {
        return new CachedApplicationUidServiceImpl(applicationUidService);
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "false", matchIfMissing = true)
    public CachedApplicationUidService emptyApplicationUidService() {
        return new EmptyApplicationUidService();
    }
}
