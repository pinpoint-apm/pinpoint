package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ApplicationUidConfig;
import com.navercorp.pinpoint.collector.uid.config.ServiceUidMysqlCacheConfig;
import com.navercorp.pinpoint.collector.uid.config.UidHbaseTemplateConfiguration;
import com.navercorp.pinpoint.service.ServiceModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ServiceModule.class,
        ServiceUidMysqlCacheConfig.class,

        ApplicationUidConfig.class,

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

}
