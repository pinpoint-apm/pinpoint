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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "pinpoint.modules.datasource.main-datasource", havingValue = "main", matchIfMissing = true)
public class MainDataSourceConfiguration {

    private final Logger logger = LogManager.getLogger(MainDataSourceConfiguration.class);

    public MainDataSourceConfiguration() {
        logger.info("Install {}", MainDataSourceConfiguration.class.getSimpleName());
    }


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig dataSourceProperties() {
        return new HikariConfig();
    }


    @Bean
    public DataSource dataSource() {
        HikariConfig config = dataSourceProperties();
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
