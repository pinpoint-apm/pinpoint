package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ServiceLookupCacheConfiguration;
import com.navercorp.pinpoint.collector.uid.config.ServiceLookupLoadProperties;
import com.navercorp.pinpoint.collector.uid.service.CachingServiceLookupService;
import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.config.ServiceMysqlConfiguration;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Configuration
//@ConditionalOnProperty(name = ObjectNameVersion.KEY, havingValue = "v4")
@ConditionalOnProperty(name = "pinpoint.collector.service.lookup.enabled", havingValue = "true")
@Import({
        ServiceMysqlConfiguration.class,
        ServiceLookupCacheConfiguration.class,
})
public class ServiceLookupConfiguration {

    public static final String EXECUTOR_NAME = "collectorServiceLookupExecutor";

    @Bean
    @Validated
    @ConfigurationProperties(prefix = "collector.service.lookup.executor")
    public ExecutorProperties collectorServiceLookupExecutorProperties() {
        ExecutorProperties properties = new ExecutorProperties();
        properties.setCorePoolSize(4);
        properties.setMaxPoolSize(8);
        properties.setQueueCapacity(1024);
        return properties;
    }

    @Bean(EXECUTOR_NAME)
    public FactoryBean<ExecutorService> collectorServiceLookupExecutor(
            @Qualifier("collectorExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
            @Qualifier("collectorServiceLookupExecutorProperties") ExecutorProperties properties) {
        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, properties);
        factory.setThreadNamePrefix(EXECUTOR_NAME);
        return factory;
    }

    @Bean
    public ServiceLookupService cachingServiceLookupService(
            @Qualifier("collectorServiceLookupCacheProperties") CaffeineCacheProperties properties,
            @Qualifier(ServiceLookupCacheConfiguration.CACHE_MANAGER_NAME) CacheManager cacheManager,
            ServiceRegistryDao serviceRegistryDao,
            @Qualifier(EXECUTOR_NAME) Executor executor,
            ServiceLookupLoadProperties loadProperties) {
        return new CachingServiceLookupService(properties, cacheManager, serviceRegistryDao, loadProperties, executor);
    }
}
