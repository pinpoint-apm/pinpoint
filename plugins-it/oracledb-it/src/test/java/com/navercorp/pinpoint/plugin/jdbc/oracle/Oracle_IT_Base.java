/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.pluginit.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.sql.Driver;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.Properties;

public abstract class Oracle_IT_Base {
    private static final JDBCDriverClass driverClass = new OracleJDBCDriverClass();
    protected static final OracleJDBCApi JDBC_API = new OracleJDBCApi(driverClass);
    protected static OracleItHelper helper;
    public static OracleContainerWithWait oracle;


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

    public static void startOracleDB(String dockerImageVersion, WaitStrategy waitStrategy) throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        oracle = new OracleContainerWithWait(dockerImageVersion);

        if (waitStrategy != null) {
            oracle.setWaitStrategy(waitStrategy);
            oracle.withStartupTimeout(Duration.ofSeconds(300));
            oracle.addEnv("DBCA_ADDITIONAL_PARAMS", "-initParams sga_target=0M pga_aggreegate_target=0M");
            oracle.withReuse(true);
        }

        oracle.start();

        setJdbcUrl(oracle.getJdbcUrl());
        setUserName(oracle.getUsername());
        setPassWord(oracle.getPassword());

        DriverProperties driverProperties = createDriverProperties();
        helper = new OracleItHelper(driverProperties);
        helper.create(JDBC_API);
    }

    protected static DriverProperties createDriverProperties() {
        DriverProperties driverProperties = new DriverProperties(getJdbcUrl(), getUserName(), getPassWord(), new Properties());
        return driverProperties;
    }

    @AfterSharedClass
    public static void sharedTearDown() throws Exception {
        if (oracle != null) {
            oracle.stop();
        }
    }

    @Before
    public void registerDriver() throws Exception {
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }

}
