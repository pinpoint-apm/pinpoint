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

    @Bean
    public PinotDataSource pinotDataSource() {
        PinotDataSourceProperties properties = pinotDataSourceProperties();
        logger.info("pinot properties {}", properties);

        PinotDataSource datasource = new PinotDataSource();
        datasource.setJdbcUrl(properties.getJdbcUrl());
        datasource.setUsername(properties.getUsername());
        datasource.setPassword(properties.getPassword());
        datasource.setBrokers(properties.getBrokers());

        return datasource;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.pinot-datasource.pinot")
    public PinotDataSourceProperties pinotDataSourceProperties() {
        return new PinotDataSourceProperties();
    }

}
