/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.pluginit.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
public abstract class MySql_IT_Base {
    private final Logger logger = LogManager.getLogger(MySql_IT_Base.class);

    protected static String JDBC_URL;
    protected static final String USERNAME = MySqlServer.USERNAME;
    protected static final String PASSWORD = MySqlServer.PASSWORD;

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        JDBC_URL = beforeAllResult.getProperty("JDBC_URL");
    }


    abstract JDBCDriverClass getJDBCDriverClass();

    public Connection getConnection(DriverProperties driverProperties) throws SQLException {
        return DriverManager.getConnection(driverProperties.getUrl(), driverProperties.getUser(), driverProperties.getPassword());
    }

    @Before
    public void before() throws Exception {
        JDBCDriverClass driverClass = getJDBCDriverClass();
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @After
    public void after() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }

    public static DriverProperties getDriverProperties() {
        return new DriverProperties(JDBC_URL, MySqlServer.USERNAME, MySqlServer.PASSWORD, new Properties());
    }

}
