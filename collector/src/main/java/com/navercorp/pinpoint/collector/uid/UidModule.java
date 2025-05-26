package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.CollectorApplicationUidConfig;
import com.navercorp.pinpoint.collector.uid.config.ServiceUidMysqlCacheConfig;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidServiceImpl;
import com.navercorp.pinpoint.collector.uid.service.EmptyApplicationUidService;
import com.navercorp.pinpoint.service.ServiceModule;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
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
        ServiceModule.class,
        ServiceUidMysqlCacheConfig.class,

        CollectorApplicationUidConfig.class,
})
public class UidModule {

    private static final Logger logger = LogManager.getLogger(UidModule.class);

    public UidModule() {
        logger.info("Install UidModule");
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
    public ApplicationUidService cachedApplicationUidService(BaseApplicationUidService baseApplicationUidService,
                                                             @Qualifier(CollectorApplicationUidConfig.APPLICATION_UID_CACHE_NAME) CacheManager cacheManager) {
        return new ApplicationUidServiceImpl(baseApplicationUidService, cacheManager);
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "false", matchIfMissing = true)
    public ApplicationUidService emptyApplicationUidService() {
        return new EmptyApplicationUidService();
    }
}
