package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUidService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

/**
 * Fallback for the disabled case, mirroring the condition of {@link ServiceLookupConfiguration}.
 * Registers a static {@link ServiceLookupService} that resolves every serviceName to
 * {@link ServiceUid#DEFAULT} without touching storage.
 * Service lookup becomes mandatory in 4.0.0, remove this configuration then.
 */
@Configuration
@ConditionalOnProperty(name = ServiceLookupConfiguration.ENABLED_KEY, havingValue = "false", matchIfMissing = true)
public class StaticServiceLookupConfiguration {

    private static final Logger logger = LogManager.getLogger(StaticServiceLookupConfiguration.class);

    public StaticServiceLookupConfiguration() {
        logger.info("Install StaticServiceLookupConfiguration");
    }

    @Bean
    public ServiceLookupService staticServiceLookupService() {
        logger.warn("Service lookup is disabled, falling back to static DEFAULT serviceUid resolution. " +
                "Set `{}=true`, it will be mandatory in 4.0.0", ServiceLookupConfiguration.ENABLED_KEY);
        return serviceName -> {
            ServiceUid serviceUid = ServiceUidService.getServiceUid(serviceName);
            return CompletableFuture.completedFuture(serviceUid);
        };
    }
}
