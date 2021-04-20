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

import com.navercorp.pinpoint.pluginit.jdbc.*;
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

    public static void startOracleDB(String dockerImageVersion, WaitStrategy waitStrategy) {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        oracle = new OracleContainerWithWait(dockerImageVersion);

        if (waitStrategy != null) {
            oracle.setWaitStrategy(waitStrategy);
            oracle.withStartupTimeout(Duration.ofSeconds(300));
            oracle.addEnv("DBCA_ADDITIONAL_PARAMS", "-initParams sga_target=0M pga_aggreegate_target=0M");
            oracle.withReuse(true);
        }

        oracle.start();

        DriverProperties driverProperties = new DriverProperties(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword(), new Properties());
        helper = new OracleItHelper(driverProperties);
    }
    
    public static void stopOracleDB() {
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
        stopOracleDB();
    }

}
