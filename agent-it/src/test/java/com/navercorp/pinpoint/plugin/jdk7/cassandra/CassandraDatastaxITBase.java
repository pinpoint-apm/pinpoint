/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk7.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.StatementWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.DefaultSqlParser;
import com.navercorp.pinpoint.common.util.SqlParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scassandra.Scassandra;
import org.scassandra.ScassandraFactory;
import org.scassandra.http.client.ActivityClient;
import org.scassandra.http.client.CurrentClient;
import org.scassandra.http.client.PrimingClient;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author HyunGil Jeong
 */
public abstract class CassandraDatastaxITBase {

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
    private static final int DEFAULT_PORT = 9042;
    private static final int DEFAULT_ADMIN_PORT = 9043;
    private static final int PORT = CassandraTestHelper.findAvailablePortOrDefault(DEFAULT_PORT);
    private static final int ADMIN_PORT = CassandraTestHelper.findAvailablePortOrDefault(DEFAULT_ADMIN_PORT);
    private static final String CASSANDRA_ADDRESS = HOST + ":" + PORT;

    private static final Scassandra SERVER = ScassandraFactory.createServer(HOST, PORT, HOST, ADMIN_PORT);

    private Cluster cluster;

    private final PrimingClient primingClient = SERVER.primingClient();
    private final ActivityClient activityClient = SERVER.activityClient();
    private final CurrentClient currentClient = SERVER.currentClient();

    @BeforeClass
    public static void startUpBeforeClass() {
        SERVER.start();
    }

    @Before
    public void setUp() {
        cluster = Cluster.builder().addContactPoint(HOST).withPort(PORT).withoutMetrics().build();

        // scassandra uses http client 4 for stub calls
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.ignoreServiceType("HTTP_CLIENT_4", "HTTP_CLIENT_4_INTERNAL");
    }

    @After
    public void tearDown() {
        if (primingClient != null) {
            primingClient.clearAllPrimes();
        }
        if (activityClient != null) {
            activityClient.clearAllRecordedActivity();
        }
        if (cluster != null) {
            cluster.close();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
        SERVER.stop();
    }

    @Test
    public void testBoundStatement() throws Exception {
        final String testId ="99";
        final String testValue = "testValue";

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        Session myKeyspaceSession = null;

        try {
            myKeyspaceSession = cluster.connect(TEST_KEYSPACE);

            // ===============================================
            // Insert Data (PreparedStatement, BoundStatement)
            PreparedStatement preparedStatement = myKeyspaceSession.prepare(CQL_INSERT);
            BoundStatement boundStatement = new BoundStatement(preparedStatement);
            boundStatement.bind(testId, testValue);
            myKeyspaceSession.execute(boundStatement);

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
            final String cqlSelect = String.format("SELECT %s, %s FROM %s WHERE %s = %s",
                    TEST_COL_ID, TEST_COL_VALUE, TEST_TABLE, TEST_COL_ID, testId);
            myKeyspaceSession.execute(cqlSelect);
            // SessionManager#execute(String) OR AbstractSession#execute(String)
            execute = sessionClass.getDeclaredMethod("execute", String.class);
            String normalizedSelectSql = SQL_PARSER.normalizedSql(cqlSelect).getNormalizedSql();
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(normalizedSelectSql, String.valueOf(testId))));

            // ====================
            // Delete Data (String)
            final String cqlDelete = String.format("DELETE FROM %s.%s WHERE %s = ?", TEST_KEYSPACE, TEST_TABLE, TEST_COL_ID);
            myKeyspaceSession.execute(cqlDelete, testId);

            verifier.printCache();
            // SessionManager#execute(String, Object[]) OR AbstractSession#execute(String, Object[])
            execute = sessionClass.getDeclaredMethod("execute", String.class, Object[].class);
            String normalizedDeleteSql = SQL_PARSER.normalizedSql(cqlDelete).getNormalizedSql();
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(normalizedDeleteSql, null)));
        } finally {
            closeSession(myKeyspaceSession);
        }
    }

    @Test
    public void testBatchStatement_and_StatementWrapper() throws Exception {
        final String testId1 = "998";
        final String testValue1 = "testValue998";
        final String testId2 = "999";
        final String testValue2 = "testValue999";

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        Session myKeyspaceSession = null;

        try {
            myKeyspaceSession = cluster.connect(TEST_KEYSPACE);

            // ===============================================
            // Insert Data 2 x (PreparedStatement, BoundStatement)
            PreparedStatement preparedStatement = myKeyspaceSession.prepare(CQL_INSERT);
            BoundStatement boundStatement1 = new BoundStatement(preparedStatement);
            boundStatement1.bind(testId1, testValue1);
            BoundStatement boundStatement2 = new BoundStatement(preparedStatement);
            boundStatement2.bind(testId2, testValue2);

            BatchStatement batchStatement = new BatchStatement();
            batchStatement.add(boundStatement1);
            batchStatement.add(boundStatement2);

            myKeyspaceSession.execute(batchStatement);

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
            PreparedStatement deletePreparedStatement = myKeyspaceSession.prepare(cqlDelete);
            BoundStatement deleteBoundStatement = new BoundStatement(deletePreparedStatement);
            deleteBoundStatement.bind(testId1, testId2);
            Statement wrappedDeleteStatement = new StatementWrapper(deleteBoundStatement) {};
            myKeyspaceSession.execute(wrappedDeleteStatement);

            verifier.printCache();
            // SessionManager#prepare(String) OR AbstractSession#prepare(String)
            verifier.verifyTrace(event(CASSANDRA, prepare, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(cqlDelete, null)));
            // SessionManager#execute(String, Object[]) OR AbstractSession#execute(String, Object[])
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE, sql(cqlDelete, null)));
        } finally {
            closeSession(myKeyspaceSession);
        }
    }

    private static void closeSession(Session session) {
        if (session != null) {
            session.close();
        }
    }

}
