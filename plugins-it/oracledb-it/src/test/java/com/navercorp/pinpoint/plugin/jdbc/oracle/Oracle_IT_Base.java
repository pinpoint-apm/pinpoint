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
import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

public abstract class Oracle_IT_Base {
    private static final JDBCDriverClass driverClass = new OracleJDBCDriverClass();
    protected static final OracleJDBCApi JDBC_API = new OracleJDBCApi(driverClass);
    protected static OracleItHelper helper;

    private static DriverProperties driverProperties;

    public static String getJdbcUrl() {
        return driverProperties.getUrl();
    }

    public static String getUserName() {
        return driverProperties.getUser();
    }

    public static String getPassWord() {
        return driverProperties.getPassword();
    }


    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        driverProperties = DatabaseContainers.readDriverProperties(beforeAllResult);
    }


    protected static DriverProperties createDriverProperties() {
        return driverProperties;
    }


    @BeforeEach
    public void registerDriver() throws Exception {
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }

}
