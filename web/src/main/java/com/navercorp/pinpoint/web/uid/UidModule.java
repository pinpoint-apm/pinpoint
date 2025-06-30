package com.navercorp.pinpoint.web.uid;

import com.navercorp.pinpoint.service.ServiceModule;
import com.navercorp.pinpoint.uid.UidCommonConfiguration;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.web.uid.config.WebApplicationUidConfig;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidServiceImpl;
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
        ServiceModule.class,
        UidCommonConfiguration.class,

        WebApplicationUidConfig.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.web.uid.controller",
        "com.navercorp.pinpoint.common.server.uid"
})
public class UidModule {

    private static final Logger logger = LogManager.getLogger(UidModule.class);

    public UidModule() {
        logger.info("Install UidModule");
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
    public ApplicationUidService cachedApplicationUidService(BaseApplicationUidService baseApplicationUidService) {
        return new ApplicationUidServiceImpl(baseApplicationUidService);
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "false", matchIfMissing = true)
    public ApplicationUidService emptyApplicationUidService() {
        return new EmptyApplicationUidService();
    }
}
