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

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import io.r2dbc.mssql.MssqlConnectionConfiguration;
import io.r2dbc.mssql.MssqlConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"io.r2dbc:r2dbc-mssql:[0.9.0]",
        "com.microsoft.sqlserver:mssql-jdbc:10.2.1.jre8",
        "org.springframework.data:spring-data-r2dbc:1.5.1",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedDependency({"com.microsoft.sqlserver:mssql-jdbc:10.2.1.jre8", PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MSSQL})
@SharedTestLifeCycleClass(MssqlServer.class)
public class R2dbcMssql_IT extends SqlBase {

    private final Logger logger = LogManager.getLogger(getClass());

    private static R2dbcEntityTemplate template;

    private final TestStatementHelper testStatementHelper = new TestStatementHelper();

    @BeforeAll
    public static void beforeClass() throws Exception {
        DriverProperties driverProperties = getDriverProperties();

        final String host = driverProperties.getProperty(DriverProperties.HOST);
        final int port = Integer.parseInt(driverProperties.getProperty(DriverProperties.PORT));
        final String username = driverProperties.getUser();
        final String password = driverProperties.getPassword();

        MssqlConnectionConfiguration connectionConfiguration = MssqlConnectionConfiguration.builder().host(host).port(port).username(username).password(password).build();
        MssqlConnectionFactory connectionFactory = new MssqlConnectionFactory(connectionConfiguration);
        template = new R2dbcEntityTemplate(connectionFactory);
    }

    @Test
    public void testStatements() throws Exception {
        testStatementHelper.sql(template);
    }
}
