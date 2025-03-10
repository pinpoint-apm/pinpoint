package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ApplicationUidConfig;
import com.navercorp.pinpoint.collector.uid.config.ServiceUidCacheConfig;
import com.navercorp.pinpoint.collector.uid.config.UidHbaseTemplateConfiguration;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.EmptyApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.ServiceGroupService;
import com.navercorp.pinpoint.collector.uid.service.StaticServiceGroupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ApplicationUidConfig.class,
        ServiceUidCacheConfig.class,

        UidHbaseTemplateConfiguration.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.collector.uid",
        "com.navercorp.pinpoint.common.server.uid"
})
public class UidModule {

    private static final Logger logger = LogManager.getLogger(UidModule.class);

    public UidModule() {
        logger.info("Install UidModule");
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationUidService.class)
    public ApplicationUidService emptyApplicationUidService() {
        return new EmptyApplicationUidService();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceGroupService.class)
    public ServiceGroupService defaultServiceGroupService(@Value("${collector.service.uid.default.value:0}") int staticServiceUid) {
        return new StaticServiceGroupService(staticServiceUid);
    }
}
