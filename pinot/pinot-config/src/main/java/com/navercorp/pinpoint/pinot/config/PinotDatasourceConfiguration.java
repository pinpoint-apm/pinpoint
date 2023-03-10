package com.navercorp.pinpoint.pinot.config;

import com.navercorp.pinpoint.pinot.datasource.PinotDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
@PropertySource("classpath:/pinot/profiles/${pinpoint.profiles.active:release}/pinot-jdbc.properties")
public class PinotDatasourceConfiguration {

    private final Logger logger = LogManager.getLogger(PinotDatasourceConfiguration.class);

    @Bean(name = "pinotDataSource", destroyMethod = "close")
    public PinotDataSource getPinotDatasource(PinotDataSourceProperties properties) {
        logger.info("pinot jdbc url:{}", properties.getUrl());
        logger.debug("pinot jdbc username:{}", properties.getUsername());

        PinotDataSource datasource = new PinotDataSource();
        datasource.setUrl(properties.getUrl());
        datasource.setUsername(properties.getUsername());
        datasource.setPassword(properties.getPassword());
        return datasource;
    }

    @Bean
    @ConfigurationProperties(prefix = "pinpoint.pinot.jdbc")
    public PinotDataSourceProperties getPinotDataSourceProperties() {
        return new PinotDataSourceProperties();
    }

}
