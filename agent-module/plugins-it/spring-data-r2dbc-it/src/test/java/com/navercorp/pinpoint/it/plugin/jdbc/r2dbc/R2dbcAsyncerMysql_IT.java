/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc.r2dbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ConnectionFactoryProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

/**
 * io.asyncer:r2dbc-mysql. 1.4.2 appended a parameter to InitFlow.initHandshake.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"io.asyncer:r2dbc-mysql:[1.1.3],[1.4.1],[1.4.3]",
        "org.springframework.data:spring-data-r2dbc:1.5.1",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedDependency({"mysql:mysql-connector-java:8.0.28", PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MYSQLDB})
@SharedTestLifeCycleClass(MysqlServer.class)
public class R2dbcAsyncerMysql_IT extends SqlBase {

    // This module compiles against dev.miku:r2dbc-mysql, which shares the artifactId with io.asyncer:r2dbc-mysql,
    // so the asyncer driver is reached through the R2DBC SPI instead of its own API.
    private static final String PROVIDER_CLASS = "io.asyncer.r2dbc.mysql.MySqlConnectionFactoryProvider";

    private static R2dbcEntityTemplate template;
    private static ConnectionFactory connectionFactory;
    private static String expectedHost;
    private static String expectedDatabase;

    private final TestStatementHelper testStatementHelper = new TestStatementHelper();

    @BeforeAll
    public static void beforeClass() throws Exception {
        DriverProperties driverProperties = getDriverProperties();

        final String host = driverProperties.getProperty(DriverProperties.HOST);
        final int port = Integer.parseInt(driverProperties.getProperty(DriverProperties.PORT));
        final String database = driverProperties.getProperty(DriverProperties.DATABASE);

        final ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "mysql")
                .option(ConnectionFactoryOptions.HOST, host)
                .option(ConnectionFactoryOptions.PORT, port)
                .option(ConnectionFactoryOptions.USER, driverProperties.getUser())
                .option(ConnectionFactoryOptions.PASSWORD, driverProperties.getPassword())
                .option(ConnectionFactoryOptions.DATABASE, database)
                .build();
        final ConnectionFactoryProvider provider = (ConnectionFactoryProvider) Class.forName(PROVIDER_CLASS).getConstructor().newInstance();
        connectionFactory = provider.create(options);
        template = new R2dbcEntityTemplate(connectionFactory);
        expectedHost = host + ":" + port;
        expectedDatabase = database;
    }

    @Test
    public void testStatements() throws Exception {
        testStatementHelper.sql(template);
    }

    @Test
    public void databaseInfo() {
        final Connection connection = Mono.from(connectionFactory.create()).block();
        Assertions.assertNotNull(connection, "connection");
        try {
            Assertions.assertTrue(connection instanceof DatabaseInfoAccessor, "Not DatabaseInfoAccessor");
            final DatabaseInfo databaseInfo = ((DatabaseInfoAccessor) connection)._$PINPOINT$_getDatabaseInfo();
            Assertions.assertNotNull(databaseInfo, "databaseInfo");
            Assertions.assertEquals(expectedDatabase, databaseInfo.getDatabaseId());
            Assertions.assertEquals(expectedHost, databaseInfo.getMultipleHost());
        } finally {
            Mono.from(connection.close()).block();
        }
    }
}
