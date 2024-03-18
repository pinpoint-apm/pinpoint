/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.jdbc.clickhouse;

import com.clickhouse.jdbc.ClickHouseDriver;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author intr3p1d
 */
public class ClickHouseITHelper {

    private final Logger logger = LogManager.getLogger(getClass());

    private final String jdbcUrl;
    private final String userName;
    private final String password;


    public ClickHouseITHelper(DriverProperties driverProperties) {
        Objects.requireNonNull(driverProperties, "driverProperties");

        this.jdbcUrl = driverProperties.getUrl();
        this.userName = driverProperties.getUser();
        this.password = driverProperties.getPassword();

    }

    public Connection getConnection() throws SQLException {
        Driver driver = new ClickHouseDriver();
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        final Connection conn = driver.connect(jdbcUrl, properties);

        logger.info("Connected to: " + conn.getMetaData().getURL());
        return conn;
    }

}

