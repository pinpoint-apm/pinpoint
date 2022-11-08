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

import io.r2dbc.mssql.MssqlConnectionConfiguration;
import io.r2dbc.mssql.MssqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Qualifier("mssql")
public class MssqlR2dbcDatabase implements R2dbcDatabase {

    private MssqlConnectionFactory connectionFactory;

    @PostConstruct
    public void init() throws Exception {
        MssqlConnectionConfiguration connectionConfiguration = MssqlConnectionConfiguration.builder()
                .host("localhost")
                .port(14363)
                .username("sa")
                .password("A_Str0ng_Required_Password")
                .build();
        connectionFactory = new MssqlConnectionFactory(connectionConfiguration);
    }

    @PreDestroy
    public void destroy() {
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
}
