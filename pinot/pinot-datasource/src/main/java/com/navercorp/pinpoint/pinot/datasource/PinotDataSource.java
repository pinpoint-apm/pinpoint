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

package com.navercorp.pinpoint.pinot.datasource;

import org.apache.pinot.client.PinotConnection;
import org.apache.pinot.client.PinotDriver;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinotDataSource implements DataSource, AutoCloseable {
    public static final String TENANT = "tenant";

    private final PinotDriver driver;
    private String url;
    private String username;
    private String password;
    private String tenant;
    private Properties connectionProperties;
    private PrintWriter logWriter;

    private volatile PinotConnection pinotConnection;

    public PinotDataSource() {
        this(new PinotDriver());
    }

    PinotDataSource(PinotDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver");
    }

    public void setUrl(String url) {
        this.url = Objects.requireNonNull(url, "url");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (pinotConnection == null) {
            synchronized (this) {
                if (pinotConnection == null) {
                    pinotConnection = newPinotConnection(url, username, password);
                }
            }
        }
        return new WrappedPinotConnection(pinotConnection);
    }

    private PinotConnection newPinotConnection(String url, String username, String password) throws SQLException {
        Properties properties;
        if (connectionProperties == null) {
            properties = new Properties();
        } else {
            properties = new Properties(connectionProperties);
        }
        
        if (username != null) {
            properties.setProperty("user", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }
        if (tenant != null) {
            properties.setProperty(TENANT, tenant);
        }

        return (PinotConnection) driver.connect(url, properties);
    }


    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("getConnection(String, String)");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        this.logWriter = logWriter;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("getLoginTimeout");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("unwrap");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public void close() throws Exception {
        if (pinotConnection == null) {
            return;
        }
        pinotConnection.close();
    }
}
