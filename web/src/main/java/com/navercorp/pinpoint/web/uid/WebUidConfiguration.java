package com.navercorp.pinpoint.web.uid;

import com.navercorp.pinpoint.service.ServiceModule;
import com.navercorp.pinpoint.web.uid.config.ServiceUidMysqlCacheConfig;
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
        "com.navercorp.pinpoint.web.uid.controller",
        "com.navercorp.pinpoint.web.uid.service",
})
public class WebUidConfiguration {

    @Bean("serviceUidCache")
    @ConditionalOnMissingBean(name = "serviceUidCache")
    public CacheManager defaultServiceUidCache() {
        return new NoOpCacheManager();
    }
}
