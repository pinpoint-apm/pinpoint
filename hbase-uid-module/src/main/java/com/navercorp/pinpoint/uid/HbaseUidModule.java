package com.navercorp.pinpoint.uid;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import com.navercorp.pinpoint.uid.config.UidHbaseTemplateConfiguration;
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
public class HbaseUidModule {

    @Bean
    public IdGenerator<ApplicationUid> applicationUidGenerator() {
        return new RandomApplicationUidGenerator();
    }

}
