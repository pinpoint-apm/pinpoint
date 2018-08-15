/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.jdk7.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.StatementWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.DefaultSqlParser;
import com.navercorp.pinpoint.common.util.SqlParser;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Iterator;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author HyunGil Jeong
 */
public abstract class CassandraDatastaxITBase {

    // com.navercorp.pinpoint.plugin.cassandra.CassandraConstants
    protected static final String CASSANDRA = "CASSANDRA";
    protected static final String CASSANDRA_EXECUTE_QUERY = "CASSANDRA_EXECUTE_QUERY";

    protected static final String TEST_KEYSPACE = "mykeyspace";
    protected static final String TEST_TABLE = "mytable";
    protected static final String TEST_COL_ID = "id";
    protected static final String TEST_COL_VALUE = "value";

    protected static final String CQL_CREATE_KEYSPACE = String.format(
            "CREATE KEYSPACE %s WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };",
            TEST_KEYSPACE);
    protected static final String CQL_CREATE_TABLE = String.format(
            "CREATE TABLE %s ( %s int PRIMARY KEY, %s text );",
            TEST_TABLE, TEST_COL_ID, TEST_COL_VALUE);
    protected static final String CQL_INSERT = String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?);",
            TEST_TABLE, TEST_COL_ID, TEST_COL_VALUE);
    // for normalized sql used for sql cache
    protected static final SqlParser SQL_PARSER = new DefaultSqlParser();

    protected static Cluster cluster;

    // see cassandra/cassandra_${cassandraVersion}.yaml
    protected static String HOST;
    protected static int PORT;
    protected static String CASSANDRA_ADDRESS;

    public static void initializeCluster(String cassandraVersion) throws Exception {
        CassandraTestHelper.init(cassandraVersion);
        HOST = CassandraTestHelper.getHost();
        PORT = CassandraTestHelper.getNativeTransportPort();
        CASSANDRA_ADDRESS = HOST + ":" + PORT;
        cluster = Cluster.builder().addContactPoint(HOST).withPort(PORT).withoutMetrics().build();
        // Create KeySpace
        Session emptySession = null;
        try {
            emptySession = cluster.connect();
            emptySession.execute(CQL_CREATE_KEYSPACE);
        } finally {
            closeSession(emptySession);
        }
        // Create Table
        Session myKeyspaceSession = null;
        try {
            myKeyspaceSession = cluster.connect(TEST_KEYSPACE);
            myKeyspaceSession.execute(CQL_CREATE_TABLE);
        } finally {
            closeSession(myKeyspaceSession);
        }
    }

    @Test
    public void testBoundStatement() throws Exception {
        final int testId = 99;
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
            final String cqlSelect = String.format("SELECT %s, %s FROM %s WHERE %s = %d",
                    TEST_COL_ID, TEST_COL_VALUE, TEST_TABLE, TEST_COL_ID, testId);
            verifySelect(myKeyspaceSession.execute(cqlSelect), testId, testValue);

            verifier.printCache();
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
        final int testId1 = 998;
        final String testValue1 = "testValue998";
        final int testId2 = 999;
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
            verifier.verifyTrace(event(CASSANDRA_EXECUTE_QUERY, execute, null, CASSANDRA_ADDRESS, TEST_KEYSPACE));
        } finally {
            closeSession(myKeyspaceSession);
        }
    }

    private void verifySelect(ResultSet rs, int expectedTestId, String expectedTestValue) {
        int resultCount = 0;
        Iterator<Row> iter = rs.iterator();
        while (iter.hasNext()) {
            Row row = iter.next();
            Assert.assertEquals(expectedTestId, row.getInt(0));
            Assert.assertEquals(expectedTestValue, row.getString(1));
            ++resultCount;
        }
        Assert.assertEquals(1, resultCount);
    }

    private static void closeSession(Session session) {
        if (session != null) {
            session.close();
        }
    }

    public static void cleanUpCluster() {
        if (cluster != null) {
            cluster.close();
        }
    }

}
