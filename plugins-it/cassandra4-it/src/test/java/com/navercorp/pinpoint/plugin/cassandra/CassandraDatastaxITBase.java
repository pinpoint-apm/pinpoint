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

package com.navercorp.pinpoint.plugin.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.CassandraContainer;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
public abstract class CassandraDatastaxITBase {
    private static final Logger logger = LogManager.getLogger(CassandraDatastaxITBase.class);
    // com.navercorp.pinpoint.plugin.cassandra.CassandraConstants
    private static final String CASSANDRA = "CASSANDRA";
    private static final String CASSANDRA_EXECUTE_QUERY = "CASSANDRA4_EXECUTE_QUERY";
    private static final String TEST_KEYSPACE = CassandraITConstants.TEST_KEYSPACE;
    private static final String TEST_TABLE = CassandraITConstants.TEST_TABLE;
    private static final String TEST_COL_ID = "id";
    private static final String TEST_COL_VALUE = "value";
    private static final String CQL_INSERT = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?);", TEST_TABLE, TEST_COL_ID, TEST_COL_VALUE);
    // for normalized sql used for sql cache
    private static final String LOCAL_DATACENTER = "datacenter1";
    private static String HOST = "127.0.0.1";
    private static String CASSANDRA_ADDRESS = HOST + ":" + CassandraContainer.CQL_PORT;

    private static int PORT;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        logger.info("CassandraServer result {}", beforeAllResult);
        PORT = Integer.parseInt(beforeAllResult.getProperty("PORT"));
    }

    public static int getPort() {
        return PORT;
    }


    @BeforeAll
    public static void setup() {
        CASSANDRA_ADDRESS = HOST + ":" + getPort();
        logger.info("setup cluster {}", CASSANDRA_ADDRESS);
    }

    @AfterAll
    public static void tearDown() {
    }

    @Test
    public void testBoundStatement() throws Exception {
        final String testId = "99";
        final String testValue = "testValue";

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(HOST, PORT)).withLocalDatacenter(LOCAL_DATACENTER).withKeyspace(TEST_KEYSPACE).build();
        // ===============================================
        Statement statement = SimpleStatement.builder(CQL_INSERT).addPositionalValue("simple").addPositionalValue("statement").build();
        session.execute(statement);

        verifier.printCache();
        // SessionManager#prepare(String) OR AbstractSession#prepare(String)
        Class<?> sessionClass = Class.forName("com.datastax.oss.driver.internal.core.session.DefaultSession");
        Method execute = sessionClass.getDeclaredMethod("execute", Request.class, GenericType.class);
//        verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(CQL_INSERT, null)));

        if (session != null) {
            session.close();
        }
    }
}
