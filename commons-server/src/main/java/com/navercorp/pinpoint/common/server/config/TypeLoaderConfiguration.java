package com.navercorp.pinpoint.common.server.config;

import com.navercorp.pinpoint.common.server.util.ServerTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
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
    public TraceMetadataLoaderService typeLoaderService() {
        return new ServerTraceMetadataLoaderService();
    }

    @Bean
    public ServiceTypeRegistryService serviceTypeRegistryService(TraceMetadataLoaderService typeLoaderService) {
        ServiceTypeLocator serviceTypeLocator = typeLoaderService.getServiceTypeLocator();
        return new DefaultServiceTypeRegistryService(serviceTypeLocator);
    }

}
