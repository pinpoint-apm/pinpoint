package com.navercorp.pinpoint.collector.uid.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheSpec;
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
    @ConfigurationProperties(prefix = "collector.application.uid.cache")
    public CaffeineCacheSpec applicationUidCacheSpec() {
        return new CaffeineCacheSpec();
    }

    @Bean
    public CacheManager applicationUidCache(@Qualifier("applicationUidCacheSpec") CaffeineCacheSpec caffeineCacheSpec) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(APPLICATION_UID_CACHE_NAME);
        cacheManager.setCaffeine(Caffeine.from(caffeineCacheSpec.getSpecification()));
        cacheManager.setAsyncCacheMode(true);
        cacheManager.setAllowNullValues(false);

        return cacheManager;
    }

    @Bean
    public IdGenerator<ApplicationUid> applicationUidGenerator() {
        return new RandomApplicationUidGenerator();
    }

}
