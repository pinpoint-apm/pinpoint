/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDataSourceConfiguration {
    private final Logger logger = LogManager.getLogger(MetaDataSourceConfiguration.class);

    public MetaDataSourceConfiguration() {
        logger.info("Install {}", MetaDataSourceConfiguration.class.getSimpleName());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.meta-datasource.hikari")
    public HikariConfig metaDataSourceProperties() {
        return new HikariConfig();
    }

    @Bean
    public DataSource metaDataDataSource() {
        HikariConfig config = metaDataSourceProperties();
        return new HikariDataSource(config);
    }

    @Bean
    public PlatformTransactionManager metaDataTransactionManager(@Qualifier("metaDataDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
