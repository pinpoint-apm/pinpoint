/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.pinot;

import com.navercorp.pinpoint.metric.web.mybatis.PinotConnectionDelegator;
import org.apache.pinot.client.PinotDriver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author minwoo.jung
 */
public class PinpointPinotDriver implements Driver {

    private final PinotDriver pinotDriver = new PinotDriver();
    private final Object connectionMonitor = new Object();
    private Connection connection;

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        synchronized(this.connectionMonitor) {
            if (this.connection == null || this.connection.isClosed()) {
                initConnection(url, info);
            }

            return this.connection;
        }
    }

    private Connection initConnection(String url, Properties info) throws SQLException {
        Connection connection = pinotDriver.connect(url, info);
        this.connection = new PinotConnectionDelegator(connection);
        return connection;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return pinotDriver.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return pinotDriver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return pinotDriver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return pinotDriver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return pinotDriver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return pinotDriver.getParentLogger();
    }
}
