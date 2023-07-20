/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.r2dbc;

import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class SqlBase {
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
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    static class JdbcUrlSplitter {
        public String driverName, host, database, params;
        public int port;

        public JdbcUrlSplitter(String jdbcUrl) {
            int pos, pos1, pos2;
            String connUri;

            if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")
                    || (pos1 = jdbcUrl.indexOf(':', 5)) == -1)
                throw new IllegalArgumentException("Invalid JDBC url.");

            driverName = jdbcUrl.substring(5, pos1);
            if ((pos2 = jdbcUrl.indexOf(';', pos1)) == -1) {
                connUri = jdbcUrl.substring(pos1 + 1);
            } else {
                connUri = jdbcUrl.substring(pos1 + 1, pos2);
                params = jdbcUrl.substring(pos2 + 1);
            }

            if (connUri.startsWith("//")) {
                if ((pos = connUri.indexOf('/', 2)) != -1) {
                    host = connUri.substring(2, pos);
                    database = connUri.substring(pos + 1);

                    if ((pos = host.indexOf(':')) != -1) {
                        port = Integer.parseInt(host.substring(pos + 1));
                        host = host.substring(0, pos);
                    }
                }
            } else {
                database = connUri;
            }
        }
    }
}
