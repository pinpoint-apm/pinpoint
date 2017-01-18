/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hikaricp;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.proxy.ConnectionProxy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Taejin Koo
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.zaxxer:HikariCP-java6:[2.3.13]", "com.h2database:h2:1.4.191"})
public class HikariCpIT {

    private static final String serviceType = "HIKARICP";

    private static HikariDataSource dataSource;

    @BeforeClass
    public static void setup() {
        final HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        dataSource = new HikariDataSource(config);
    }

    @AfterClass
    public static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    public void testIdleTimeout4() throws InterruptedException, SQLException, NoSuchMethodException {
        Connection connection = dataSource.getConnection();
        Assert.assertNotNull(connection);

        Thread.sleep(500);

        connection.close();

        Thread.sleep(500);

        Method getConnection = HikariDataSource.class.getDeclaredMethod("getConnection");
        Method proxyConnection = ConnectionProxy.class.getDeclaredMethod("close");

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event(serviceType, getConnection));
        verifier.verifyTrace(event(serviceType, proxyConnection));
    }

}
