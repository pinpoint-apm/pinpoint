package com.navercorp.pinpoint.pinot.config;

import com.navercorp.pinpoint.common.server.tenant.SimpleTenantProvider;
import com.navercorp.pinpoint.common.server.tenant.TenantProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
@PropertySource("classpath:/pinot/profiles/${pinpoint.profiles.active:release}/pinot-tenant.properties")
public class PinotTenantProviderConfiguration {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Bean
    public TenantProvider getTenantProvider(@Value("${pinpoint.pinot.tenantId:pinpoint}") String tenantId) {
        logger.debug("pinot tenantId:{}", tenantId);
        return new SimpleTenantProvider(tenantId);
    }

}
