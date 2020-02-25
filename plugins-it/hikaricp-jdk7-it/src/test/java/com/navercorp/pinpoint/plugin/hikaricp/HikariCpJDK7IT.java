/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hikaricp;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.proxy.ConnectionProxy;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author Roy Kim
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.zaxxer:HikariCP-java6:[2.3.10,2.3.11]", "com.h2database:h2:1.4.191"})
public class HikariCpJDK7IT {

    private static final String serviceType = "HIKARICP";
    private static final String DATA_SOURCE_CLASS_NAME = "org.h2.jdbcx.JdbcDataSource";
    private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    private static Method getConnectionMethod1;
    private static Method getConnectionMethod2;
    private static Method proxyConnectionMethod;

    @BeforeClass
    public static void setUp() throws NoSuchMethodException {
        getConnectionMethod1 = HikariDataSource.class.getDeclaredMethod("getConnection");
        getConnectionMethod2 = HikariDataSource.class.getDeclaredMethod("getConnection", String.class, String.class);
        proxyConnectionMethod = ConnectionProxy.class.getDeclaredMethod("close");
    }

    @Test
    public void defaultTest1() throws InterruptedException, SQLException, NoSuchMethodException {
        final HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(DATA_SOURCE_CLASS_NAME);
        config.addDataSourceProperty("url", JDBC_URL);

        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            Connection connection = dataSource.getConnection();
            Assert.assertNotNull(connection);

            Thread.sleep(500);

            connection.close();

            Thread.sleep(500);

            Constructor<HikariDataSource> constructor = HikariDataSource.class.getConstructor(HikariConfig.class);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();

            verifier.verifyTrace(event(serviceType, "com.zaxxer.hikari.HikariDataSource.HikariDataSource(com.zaxxer.hikari.HikariConfig)"));
            verifier.verifyTrace(event(serviceType, "com.zaxxer.hikari.pool.BaseHikariPool.BaseHikariPool(com.zaxxer.hikari.HikariConfig, java.lang.String, java.lang.String)"));
            verifier.verifyTrace(event(serviceType, getConnectionMethod1));
            verifier.verifyTrace(event(serviceType, proxyConnectionMethod));
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    @Test
    public void defaultTest2() throws InterruptedException, SQLException, NoSuchMethodException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDataSourceClassName(DATA_SOURCE_CLASS_NAME);
        dataSource.addDataSourceProperty("url", JDBC_URL);

        try {
            Connection connection = dataSource.getConnection();
            Assert.assertNotNull(connection);

            Thread.sleep(500);

            connection.close();

            Thread.sleep(500);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();

            verifier.verifyTrace(event(serviceType, "com.zaxxer.hikari.HikariDataSource.HikariDataSource()"));
            verifier.verifyTrace(event(serviceType, getConnectionMethod1));
            verifier.verifyTrace(event(serviceType, "com.zaxxer.hikari.pool.BaseHikariPool.BaseHikariPool(com.zaxxer.hikari.HikariConfig, java.lang.String, java.lang.String)"));
            verifier.verifyTrace(event(serviceType, proxyConnectionMethod));

        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    @Test
    public void defaultTest3() throws InterruptedException, SQLException, NoSuchMethodException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDataSourceClassName(DATA_SOURCE_CLASS_NAME);
        dataSource.addDataSourceProperty("url", JDBC_URL);

        try {
            Connection connection = dataSource.getConnection("", "");
            Assert.assertNotNull(connection);

            Thread.sleep(500);

            connection.close();

            Thread.sleep(500);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();

            verifier.verifyTrace(event(serviceType, "com.zaxxer.hikari.HikariDataSource.HikariDataSource()"));
            verifier.verifyTrace(event(serviceType, getConnectionMethod2, annotation(AnnotationKey.ARGS0.getName(), "")));
            verifier.verifyTrace(event(serviceType, proxyConnectionMethod));
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

}
