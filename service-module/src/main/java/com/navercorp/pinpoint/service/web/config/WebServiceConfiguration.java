package com.navercorp.pinpoint.service.web.config;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomServiceUidGenerator;
import com.navercorp.pinpoint.service.component.ReservedServiceRegistry;
import com.navercorp.pinpoint.service.config.ServiceMysqlConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ServiceMysqlConfiguration.class,
        ServiceParamConfiguration.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.service.service",
        "com.navercorp.pinpoint.service.web.controller",
})
public class WebServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "serviceUidGenerator")
    public IdGenerator<ServiceUid> serviceUidGenerator() {
        return new RandomServiceUidGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReservedServiceRegistry reservedServiceRegistry() {
        return new ReservedServiceRegistry();
    }
}
