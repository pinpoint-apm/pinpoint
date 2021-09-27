/*
 * Copyright 2020 NAVER Corp.
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
 */

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.pluginit.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class PostgreSqlBase {
    protected abstract JDBCDriverClass getJDBCDriverClass();

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlBase.class);

    protected static JdbcDatabaseContainer container;

    // ---------- For @BeforeSharedClass, @AfterSharedClass   //
    private static String JDBC_URL;
    private static String USER_NAME;
    private static String PASS_WORD;

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static void setJdbcUrl(String jdbcUrl) {
        JDBC_URL = jdbcUrl;
    }

    public static String getUserName() {
        return USER_NAME;
    }

    public static void setUserName(String userName) {
        USER_NAME = userName;
    }

    public static String getPassWord() {
        return PASS_WORD;
    }

    public static void setPassWord(String passWord) {
        PASS_WORD = passWord;
    }
    // ---------- //

    @AfterSharedClass
    public static void sharedTeardown() throws Exception {
        if (container != null) {
            container.stop();
        }
    }

    public static DriverProperties getDriverProperties() {
        DriverProperties driverProperties = new DriverProperties(getJdbcUrl(), getUserName(), getPassWord(), new Properties());
        return driverProperties;
    }


    @Before
    public void registerDriver() throws Exception {
        Driver driver = getJDBCDriverClass().getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @After
    public void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }
}
