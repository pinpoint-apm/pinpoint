package com.navercorp.pinpoint.collector.uid.config;


import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.cache.UidCaffeineCacheBuilder;
import com.navercorp.pinpoint.common.server.uid.cache.UidCaffeineCacheProperties;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class ApplicationUidConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ApplicationUidConfig() {
        logger.info("Install {}", ApplicationUidConfig.class.getSimpleName());
    }

    public static final String APPLICATION_UID_CACHE_NAME = "applicationUidCache";

    @Bean
    @ConfigurationProperties(prefix = "pinpoint.application.uid.cache")
    public UidCaffeineCacheProperties applicationUidCacheProperties() {
        return new UidCaffeineCacheProperties();
    }

    @Bean
    public CacheManager applicationUidCache(@Qualifier("applicationUidCacheProperties") UidCaffeineCacheProperties uidCaffeineCacheProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(APPLICATION_UID_CACHE_NAME);
        cacheManager.setCaffeine(new UidCaffeineCacheBuilder()
                .build(uidCaffeineCacheProperties)
        );
        return cacheManager;
    }

    @Bean
    public IdGenerator<ApplicationUid> applicationIdGenerator() {
        return new RandomApplicationUidGenerator();
    }

}
