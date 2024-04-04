/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;


@Component
@Qualifier("h2")
public class H2R2dbcDatabase implements R2dbcDatabase {

    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() throws Exception {
        connectionFactory = new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .inMemory("test").username("sa").password("").build());

        R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);
        DatabaseClient databaseClient = template.getDatabaseClient();
        databaseClient.sql("CREATE TABLE IF NOT EXISTS persons (first_name VARCHAR(255), last_name VARCHAR(255), age INTEGER)")
                .fetch()
                .rowsUpdated()
                .subscribe(data -> System.out.println("create=" + data), error -> System.out.println("error=" + error));


    }

    @PreDestroy
    public void destroy() {
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
