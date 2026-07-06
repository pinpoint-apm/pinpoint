package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomServiceUidGenerator;
import com.navercorp.pinpoint.service.config.ServiceMysqlConfiguration;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.service.ServiceRegistryServiceImpl;
import com.navercorp.pinpoint.web.applicationmap.servicemap.ServiceResolver;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Import({
        ServiceMysqlConfiguration.class,
})
@ComponentScan(
        basePackages = {
                "com.navercorp.pinpoint.service.service",
        },
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ServiceRegistryServiceImpl.class)
        }
)
public class BatchServiceModule {

    @Bean
    @ConditionalOnMissingBean(ServiceResolver.class)
    public ServiceResolver serviceResolver() {
        return ServiceResolver.emptyResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceUidGenerator")
    public IdGenerator<ServiceUid> serviceUidGenerator() {
        return new RandomServiceUidGenerator();
    }

    @Bean
    public ServiceModelResolver serviceModelResolver(ServiceRegistryService serviceRegistryService) {
        return new ServiceModelResolver(serviceRegistryService);
    }
}
