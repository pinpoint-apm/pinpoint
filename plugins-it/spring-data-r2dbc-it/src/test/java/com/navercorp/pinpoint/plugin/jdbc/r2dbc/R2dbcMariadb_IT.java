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

package com.navercorp.pinpoint.plugin.jdbc.r2dbc;

import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-spring-data-r2dbc-plugin")
@Dependency({"org.mariadb:r2dbc-mariadb:[1.1.2]",
        "org.mariadb.jdbc:mariadb-java-client:3.0.6",
        "org.springframework.data:spring-data-r2dbc:1.5.1",
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MARIADB})
@SharedTestLifeCycleClass(MariadbServer.class)
public class R2dbcMariadb_IT extends SqlBase {

    private final Logger logger = LogManager.getLogger(getClass());

    private static R2dbcEntityTemplate template;

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
        MariadbConnectionFactory connectionFactory = new MariadbConnectionFactory(connectionConfiguration);
        template = new R2dbcEntityTemplate(connectionFactory);
    }

    @Test
    public void testStatements() throws Exception {
        testStatementHelper.sql(template);
    }
}
