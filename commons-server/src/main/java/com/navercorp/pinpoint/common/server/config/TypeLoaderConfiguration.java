package com.navercorp.pinpoint.common.server.config;

import com.navercorp.pinpoint.common.server.util.Log4j2CommonLoggerFactory;
import com.navercorp.pinpoint.common.server.util.ServerTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.loader.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class TypeLoaderConfiguration {

    @Bean
    public CommonLoggerFactory commonLoggerFactory() {
        return new Log4j2CommonLoggerFactory();
    }

    @Bean
    public TraceMetadataLoaderService typeLoaderService(CommonLoggerFactory commonLoggerFactory) {
        return new ServerTraceMetadataLoaderService(commonLoggerFactory);
    }

    @Bean
    public ServiceTypeRegistryService serviceTypeRegistryService(TraceMetadataLoaderService typeLoaderService) {
        return new DefaultServiceTypeRegistryService(typeLoaderService);
    }

}
