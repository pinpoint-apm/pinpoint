/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.it.plugin.commons.dbcp;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

/**
 * @author Jongho Moon
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-commons-dbcp-plugin", "com.navercorp.pinpoint:pinpoint-mysql-jdbc-driver-plugin", JDBCTestConstants.VERSION})
@Dependency({"commons-dbcp:commons-dbcp:[1.2,)", "com.mysql:mysql-connector-j:8.4.0", JDBCTestConstants.VERSION})
@SharedDependency({"mysql:mysql-connector-java:8.0.28", JDBCTestConstants.VERSION, TestcontainersOption.MYSQLDB})
@SharedTestLifeCycleClass(MySqlServer8.class)
public class CommonsDbcpIT {
    private static final String DBCP = "DBCP";


    @Test
    public void test() throws Exception {
        DriverProperties properties = DatabaseContainers.readSystemProperties();

        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName(System.getProperty("mysql.driverClassName"));
        source.setUrl(properties.getUrl());
        source.setUsername(properties.getUser());
        source.setPassword(properties.getPassword());
        
        Connection connection = source.getConnection();
        connection.close();
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        
        verifier.printCache();
        verifier.ignoreServiceType("MYSQL", "MYSQL_EXECUTE_QUERY");
        
        verifier.verifyTrace(Expectations.event(DBCP, BasicDataSource.class.getMethod("getConnection")));
        verifier.verifyTrace(Expectations.event(DBCP, connection.getClass().getMethod("close")));
        
        verifier.verifyTraceCount(0);
    }
}
