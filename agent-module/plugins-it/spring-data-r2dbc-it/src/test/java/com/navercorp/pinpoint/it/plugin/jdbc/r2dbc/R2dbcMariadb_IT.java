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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

@PluginTest
@PinpointAgent(AgentPath.PATH)
// 1.2.0 changed the MariadbConnectionConfiguration constructor parameters; 1.4.1 moved to Netty 4.2.
@Dependency({"org.mariadb:r2dbc-mariadb:[1.1.2],[1.2.0],[1.4.2]",
        "org.mariadb.jdbc:mariadb-java-client:3.0.6",
        "org.springframework.data:spring-data-r2dbc:1.5.1",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedDependency({"org.mariadb.jdbc:mariadb-java-client:3.0.6", PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MARIADB})
@SharedTestLifeCycleClass(MariadbServer.class)
public class R2dbcMariadb_IT extends SqlBase {

    private final Logger logger = LogManager.getLogger(getClass());

    private static R2dbcEntityTemplate template;
    private static MariadbConnectionFactory connectionFactory;
    private static String expectedHost;
    private static String expectedDatabase;

    private final TestStatementHelper testStatementHelper = new TestStatementHelper();

    @BeforeAll
    public static void beforeClass() throws Exception {
        DriverProperties driverProperties = getDriverProperties();

        final String host = driverProperties.getProperty(DriverProperties.HOST);
        final int port = Integer.parseInt(driverProperties.getProperty(DriverProperties.PORT));
        final String database = driverProperties.getProperty(DriverProperties.DATABASE);
        final String username = driverProperties.getUser();
        final String password = driverProperties.getPassword();

        MariadbConnectionConfiguration connectionConfiguration = MariadbConnectionConfiguration.builder().host(host).port(port).username(username).password(password).database(database).build();
        connectionFactory = new MariadbConnectionFactory(connectionConfiguration);
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
