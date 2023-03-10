package com.navercorp.pinpoint.pinot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PinotDatasourceConfiguration.class, PinotTenantProviderConfiguration.class})
public class PinotConfiguration {
}
