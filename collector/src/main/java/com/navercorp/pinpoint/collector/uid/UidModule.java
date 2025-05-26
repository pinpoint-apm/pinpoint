package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.CollectorApplicationUidConfig;
import com.navercorp.pinpoint.collector.uid.config.CollectorServiceUidConfig;
import com.navercorp.pinpoint.collector.uid.service.CachedApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.CachedApplicationUidServiceImpl;
import com.navercorp.pinpoint.collector.uid.service.EmptyApplicationUidService;
import com.navercorp.pinpoint.uid.UidCommonConfiguration;
import com.navercorp.pinpoint.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.uid.service.async.AsyncApplicationUidService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        UidCommonConfiguration.class,

        CollectorApplicationUidConfig.class,
        CollectorServiceUidConfig.class,
})
public class UidModule {
    private static final Logger logger = LogManager.getLogger(UidModule.class);

    public UidModule() {
        logger.info("Install UidModule");
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
    public CachedApplicationUidService cachedApplicationUidService(ApplicationUidService applicationUidService,
                                                                   AsyncApplicationUidService asyncApplicationUidService,
                                                                   @Qualifier(CollectorApplicationUidConfig.APPLICATION_UID_CACHE_NAME) CacheManager cacheManager) {
        return new CachedApplicationUidServiceImpl(applicationUidService, asyncApplicationUidService, cacheManager);
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "false", matchIfMissing = true)
    public CachedApplicationUidService emptyApplicationUidService() {
        return new EmptyApplicationUidService();
    }
}
