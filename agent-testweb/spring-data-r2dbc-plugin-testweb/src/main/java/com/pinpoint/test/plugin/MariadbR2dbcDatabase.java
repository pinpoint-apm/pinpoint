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

import io.r2dbc.spi.ConnectionFactory;
import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Qualifier("mariadb")
public class MariadbR2dbcDatabase implements R2dbcDatabase {

    private MariadbConnectionFactory connectionFactory;

    @PostConstruct
    public void init() throws Exception {
        MariadbConnectionConfiguration connectionConfiguration = MariadbConnectionConfiguration.builder().host("localhost").port(9115).username("root").password("").database("test").build();
        connectionFactory = new MariadbConnectionFactory(connectionConfiguration);
    }

    @PreDestroy
    public void destroy() {
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
}
