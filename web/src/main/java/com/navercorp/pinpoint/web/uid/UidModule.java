package com.navercorp.pinpoint.web.uid;

import com.navercorp.pinpoint.service.ServiceModule;
import com.navercorp.pinpoint.web.uid.config.ApplicationUidConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ServiceModule.class,
        ApplicationUidConfig.class,
})
@ComponentScan({
        "com.navercorp.pinpoint.web.uid",
        "com.navercorp.pinpoint.common.server.uid"
})
public class UidModule {

    private static final Logger logger = LogManager.getLogger(UidModule.class);

    public UidModule() {
        logger.info("Install UidModule");
    }
}
