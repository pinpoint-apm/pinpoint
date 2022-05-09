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
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @author Taejin Koo
 */
public abstract class MySql_IT_Base {
    private static final Logger logger = LogManager.getLogger(MySql_IT_Base.class);
    public static MySQLContainer mysqlDB = new MySQLContainer();

    protected static final String DATABASE_NAME = "test";
    protected static final String USERNAME = "root";
    protected static final String PASSWORD = "";


    // ---------- For @BeforeSharedClass, @AfterSharedClass   //
    // for shared test'
    protected static String JDBC_URL;
    protected static String URL;

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static void setJdbcUrl(String jdbcUrl) {
        JDBC_URL = jdbcUrl;
    }

    public static String getURL() {
        return URL;
    }

    public static void setURL(String URL) {
        MySql_IT_Base.URL = URL;
    }
    // ---------- //

    @BeforeSharedClass
    public static void sharedSetUp() throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        mysqlDB.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(outputFrame.getUtf8String());
            }
        });
        mysqlDB.withDatabaseName(DATABASE_NAME);
        mysqlDB.withUsername(USERNAME);
        mysqlDB.withPassword(PASSWORD);
        mysqlDB.withInitScript("init.sql");
//            mysqlDB.
        mysqlDB.withUrlParam("serverTimezone", "UTC");
        mysqlDB.withUrlParam("useSSL", "false");
        mysqlDB.start();

        setJdbcUrl(mysqlDB.getJdbcUrl());
        int port = mysqlDB.getMappedPort(3306);
        setURL(mysqlDB.getHost() + ":" + port);
    }

    @AfterSharedClass
    public static void sharedTearDown() throws Exception {
        if (mysqlDB != null) {
            mysqlDB.stop();
        }
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
        return new DriverProperties(JDBC_URL, USERNAME, PASSWORD, new Properties());
    }

}
