package com.navercorp.pinpoint.pinot.config;

import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PinotDatasourceConfiguration.class, PinotTenantProviderConfiguration.class})
public class PinotConfiguration {
    @Bean
    public MyBatisConfigurationCustomizer pinotConfigurationCustomizer() {
        return new PinotMyBatisConfigurationCustomizer();
    }
}
