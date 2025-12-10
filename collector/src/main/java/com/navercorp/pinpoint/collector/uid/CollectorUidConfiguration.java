package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ServiceUidMysqlCacheConfig;
import com.navercorp.pinpoint.service.ServiceModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ServiceModule.class,
        ServiceUidMysqlCacheConfig.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.collector.uid.service",
})
public class CollectorUidConfiguration {

    @Bean("serviceUidCache")
    @ConditionalOnMissingBean(name = "serviceUidCache")
    public CacheManager defaultServiceUidCache() {
        return new NoOpCacheManager();
    }
}
