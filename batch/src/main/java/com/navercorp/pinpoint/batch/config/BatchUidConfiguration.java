package com.navercorp.pinpoint.batch.config;

import com.navercorp.pinpoint.service.ServiceModule;
import com.navercorp.pinpoint.uid.HbaseUidModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration

@Import({
        ServiceModule.class,
        HbaseUidModule.class
})
public class BatchUidConfiguration {
}
