package com.navercorp.pinpoint.uid;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import com.navercorp.pinpoint.common.server.util.RandomServiceUidGenerator;
import com.navercorp.pinpoint.uid.config.UidHbaseTemplateConfiguration;
import com.navercorp.pinpoint.uid.dao.ServiceNameDao;
import com.navercorp.pinpoint.uid.dao.ServiceUidDao;
import com.navercorp.pinpoint.uid.service.ServiceGroupService;
import com.navercorp.pinpoint.uid.service.ServiceGroupServiceImpl;
import com.navercorp.pinpoint.uid.service.ServiceGroupV3Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        UidHbaseTemplateConfiguration.class,
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.uid.mapper",
        "com.navercorp.pinpoint.uid.dao",
        "com.navercorp.pinpoint.uid.service",
})
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class UidCommonConfiguration {

    @Bean
    public IdGenerator<ApplicationUid> applicationUidGenerator() {
        return new RandomApplicationUidGenerator();
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.v4.enabled", havingValue = "false", matchIfMissing = true)
    public ServiceGroupService serviceGroupV3Service() {
        return new ServiceGroupV3Service();
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.v4.enabled", havingValue = "true")
    public IdGenerator<ServiceUid> serviceUidGenerator() {
        return new RandomServiceUidGenerator();
    }

    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.v4.enabled", havingValue = "true")
    public ServiceGroupService serviceGroupService(ServiceUidDao serviceUidDao,
                                                   ServiceNameDao serviceNameDao,
                                                   IdGenerator<ServiceUid> serviceUidGenerator) {
        return new ServiceGroupServiceImpl(serviceUidDao, serviceNameDao, serviceUidGenerator);
    }

}
