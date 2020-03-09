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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.StatementWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlParser;
import com.navercorp.pinpoint.common.profiler.sql.SqlParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerMachineClient;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author HyunGil Jeong
 */
public abstract class CassandraDatastaxIT {

    // com.navercorp.pinpoint.plugin.cassandra.CassandraConstants
    private static final String CASSANDRA = "CASSANDRA";
    private static final String CASSANDRA_EXECUTE_QUERY = "CASSANDRA_EXECUTE_QUERY";

    private static final String TEST_KEYSPACE = "mykeyspace";
    private static final String TEST_TABLE = "mytable";
    private static final String TEST_COL_ID = "id";
    private static final String TEST_COL_VALUE = "value";

    private static final String CQL_INSERT = String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?);",
            TEST_TABLE, TEST_COL_ID, TEST_COL_VALUE);
    // for normalized sql used for sql cache
    private static final SqlParser SQL_PARSER = new DefaultSqlParser();

    private static String HOST = "127.0.0.1";

    private static String CASSANDRA_ADDRESS = HOST + ":" + CassandraContainer.CQL_PORT;

    public static final CassandraContainer cassandra = new CassandraContainer();

    private static Cluster cluster;

    @BeforeClass
    public static void startUpBeforeClass() {
        Assume.assumeTrue("Docker not enabled", DockerMachineClient.instance().isInstalled());

        cassandra.start();

//        String containerIpAddress = cassandra.getContainerIpAddress();
        final int port = cassandra.getMappedPort(CassandraContainer.CQL_PORT);
        CASSANDRA_ADDRESS = HOST + ":" + port;
        cluster = newCluster(HOST, port);
        init(cluster);
    }

    private static Cluster newCluster(String host, int port) {
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoint(host);
        builder.withPort(port);
        builder.withoutMetrics();
        return builder.build();
    }

    public static void init(Cluster cluster) {
        try (Session systemSession = cluster.connect()) {
            String createKeyspace = String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = " +
                    "{'class':'SimpleStrategy','replication_factor':'1'};", TEST_KEYSPACE);
            systemSession.execute(createKeyspace);
            String createTable = String.format("CREATE TABLE %s.%s (id text, value text, PRIMARY KEY(id))", TEST_KEYSPACE, TEST_TABLE);
            systemSession.execute(createTable);
        }
    }

    private static Session openSession(Cluster cluster) {
        return cluster.connect(TEST_KEYSPACE);
    }

    @Before
    public void setUp() {
        // scassandra uses http client 4 for stub calls
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.ignoreServiceType("HTTP_CLIENT_4", "HTTP_CLIENT_4_INTERNAL");
    }

    @After
    public void tearDown() {
    }

    @AfterClass
    public static void tearDownAfterClass() {
        cassandra.stop();
    }

    @Test
    public void testBoundStatement() throws Exception {
        final String testId ="99";
        final String testValue = "testValue";

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        try (Session session = openSession(cluster)){

            // ===============================================
            // Insert Data (PreparedStatement, BoundStatement)
            PreparedStatement preparedStatement = session.prepare(CQL_INSERT);
            BoundStatement boundStatement = new BoundStatement(preparedStatement);
            boundStatement.bind(testId, testValue);
            session.execute(boundStatement);

            verifier.printCache();
            // Cluster#connect(String)
            Class<?> clusterClass = Class.forName("com.datastax.driver.core.Cluster");
            Method connect = clusterClass.getDeclaredMethod("connect", String.class);
            verifier.verifyTrace(event(CASSANDRA, connect, null, CASSANDRA_ADDRESS, TEST_KEYSPACE));
            // SessionManager#prepare(String) OR AbstractSession#prepare(String)
            Class<?> sessionClass;
            try {
                sessionClass = Class.forName("com.datastax.driver.core.AbstractSession");
            } catch (ClassNotFoundException e) {
                sessionClass = Class.forName("com.datastax.driver.core.SessionManager");
            }
            Method prepare = sessionClass.getDeclaredMethod("prepare", String.class);
            verifier.verifyTrace(event(CASSANDRA, prepare, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(CQL_INSERT, null)));
            // SessionManager#execute(Statement) OR AbstractSession#execute(Statement)
            Method execute = sessionClass.getDeclaredMethod("execute", Statement.class);
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(CQL_INSERT, null)));

            // ====================
            // Select Data (String)
            final String cqlSelect = String.format("SELECT %s, %s FROM %s WHERE %s = '%s'",
                    TEST_COL_ID, TEST_COL_VALUE, TEST_TABLE, TEST_COL_ID, testId);
            session.execute(cqlSelect);
            // SessionManager#execute(String) OR AbstractSession#execute(String)
            execute = sessionClass.getDeclaredMethod("execute", String.class);
            String normalizedSelectSql = SQL_PARSER.normalizedSql(cqlSelect).getNormalizedSql();
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(normalizedSelectSql, String.valueOf(testId))));

            // ====================
            // Delete Data (String)
            final String cqlDelete = String.format("DELETE FROM %s.%s WHERE %s = ?", TEST_KEYSPACE, TEST_TABLE, TEST_COL_ID);
            session.execute(cqlDelete, testId);

            verifier.printCache();
            // SessionManager#execute(String, Object[]) OR AbstractSession#execute(String, Object[])
            execute = sessionClass.getDeclaredMethod("execute", String.class, Object[].class);
            String normalizedDeleteSql = SQL_PARSER.normalizedSql(cqlDelete).getNormalizedSql();
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(normalizedDeleteSql, null)));
        }
    }

    @Test
    public void testBatchStatement_and_StatementWrapper() throws Exception {
        final String testId1 = "998";
        final String testValue1 = "testValue998";
        final String testId2 = "999";
        final String testValue2 = "testValue999";

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        try (Session session = openSession(cluster)){

            // ===============================================
            // Insert Data 2 x (PreparedStatement, BoundStatement)
            PreparedStatement preparedStatement = session.prepare(CQL_INSERT);
            BoundStatement boundStatement1 = new BoundStatement(preparedStatement);
            boundStatement1.bind(testId1, testValue1);
            BoundStatement boundStatement2 = new BoundStatement(preparedStatement);
            boundStatement2.bind(testId2, testValue2);

            BatchStatement batchStatement = new BatchStatement();
            batchStatement.add(boundStatement1);
            batchStatement.add(boundStatement2);

            session.execute(batchStatement);

            verifier.printCache();
            // Cluster#connect(String)
            Class<?> clusterClass = Class.forName("com.datastax.driver.core.Cluster");
            Method connect = clusterClass.getDeclaredMethod("connect", String.class);
            verifier.verifyTrace(event(CASSANDRA, connect, null, CASSANDRA_ADDRESS, TEST_KEYSPACE));
            // SessionManager#prepare(String) OR AbstractSession#prepare(String)
            Class<?> sessionClass;
            try {
                sessionClass = Class.forName("com.datastax.driver.core.AbstractSession");
            } catch (ClassNotFoundException e) {
                sessionClass = Class.forName("com.datastax.driver.core.SessionManager");
            }
            Method prepare = sessionClass.getDeclaredMethod("prepare", String.class);
            verifier.verifyTrace(event(CASSANDRA, prepare, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(CQL_INSERT, null)));
            // SessionManager#execute(Statement) OR AbstractSession#execute(Statement)
            Method execute = sessionClass.getDeclaredMethod("execute", Statement.class);
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE));

            // ====================
            final String cqlDelete = String.format("DELETE FROM %s.%s WHERE %s IN (? , ?)", TEST_KEYSPACE, TEST_TABLE, TEST_COL_ID);
            PreparedStatement deletePreparedStatement = session.prepare(cqlDelete);
            BoundStatement deleteBoundStatement = new BoundStatement(deletePreparedStatement);
            deleteBoundStatement.bind(testId1, testId2);
            Statement wrappedDeleteStatement = new StatementWrapper(deleteBoundStatement) {};
            session.execute(wrappedDeleteStatement);

            verifier.printCache();
            // SessionManager#prepare(String) OR AbstractSession#prepare(String)
            verifier.verifyTrace(event(CASSANDRA, prepare, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(cqlDelete, null)));
            // SessionManager#execute(String, Object[]) OR AbstractSession#execute(String, Object[])
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(cqlDelete, null)));
        }
    }

}
