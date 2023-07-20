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
import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class PostgreSqlBase {
    protected abstract JDBCDriverClass getJDBCDriverClass();

    private final Logger logger = LogManager.getLogger(getClass());

    protected static DriverProperties driverProperties;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        driverProperties = DatabaseContainers.readDriverProperties(beforeAllResult);
    }

    public static DriverProperties getDriverProperties() {
        return driverProperties;
    }

    @BeforeEach
    public void registerDriver() throws Exception {
        Driver driver = getJDBCDriverClass().getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @AfterEach
    public void tearDown() throws Exception {
        DriverManagerUtils.deregisterDriver();
    }
}
