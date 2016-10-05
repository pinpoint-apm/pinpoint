package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

@RunWith(PinpointPluginTestSuite.class)
@Dependency({"org.xerial:sqlite-jdbc:3.8.11.2"})
@PinpointConfig("jdbc/sqlite/pinpoint-sqlite-test.config")
public class SqlitePlugin_3_8_x_IT extends SqlitePlugin_IT_Base {

    @Test
    public void statement() throws Exception {
        executeStmt();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Class<?> driverClass = Class.forName("org.sqlite.JDBC");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(SQLITE, connect, null, "", MEMORY, cachedArgs(JDBC_PREFIX)));

        Class<?> statementClass = Class.forName("org.sqlite.jdbc4.JDBC4Statement");
        Method execute = statementClass.getMethod("execute", String.class);
        Method executeUpdate = statementClass.getMethod("executeUpdate", String.class);
        Method executeQuery = statementClass.getMethod("executeQuery", String.class);

        verifier.verifyTrace(event(SQLITE_EXECUTE_QUERY, execute, null, "", MEMORY, sql(SQL_CREATE_TABLE, null)),
            event(SQLITE_EXECUTE_QUERY, executeUpdate, null, "", MEMORY, sql(STMT_INSERT, "1,leo", new Object[]{} )),
            event(SQLITE_EXECUTE_QUERY, executeUpdate, null, "", MEMORY, sql(STMT_INSERT, "2,yui", new Object[]{} )),
            event(SQLITE_EXECUTE_QUERY, executeQuery, null, "", MEMORY, sql(SQL_SELECT, null))
            );
    }

    @Test
    public void pstmt() throws Exception {
        executePstmt();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Class<?> driverClass = Class.forName("org.sqlite.JDBC");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(SQLITE, connect, null, "", MEMORY, cachedArgs(JDBC_PREFIX)));

        Class<?> connectionClass = Class.forName("org.sqlite.jdbc4.JDBC4Connection");
        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class, int.class, int.class, int.class);
        verifier.verifyTrace(event(SQLITE, prepareStatement, null, "", MEMORY, sql(SQL_CREATE_TABLE, null)));

        Class<?> preparedStatementClass = Class.forName("org.sqlite.jdbc4.JDBC4PreparedStatement");
        Method execute = preparedStatementClass.getMethod("execute");
        Method executeUpdate = preparedStatementClass.getMethod("executeUpdate");
        Method executeQuery = preparedStatementClass.getMethod("executeQuery");

        verifier.verifyTrace(event(SQLITE_EXECUTE_QUERY, execute, null, "", MEMORY, sql(SQL_CREATE_TABLE, null)));
        verifier.verifyTrace(event(SQLITE, prepareStatement, null, "", MEMORY, sql(PSTMT_INSERT, null)),
            event(SQLITE_EXECUTE_QUERY, executeUpdate, null, "", MEMORY, sql(PSTMT_INSERT, null, new Object[]{1, "leo"} )),
            event(SQLITE_EXECUTE_QUERY, executeUpdate, null, "", MEMORY, sql(PSTMT_INSERT, null, new Object[]{2, "yui"} )));
        verifier.verifyTrace(event(SQLITE, prepareStatement, null, "", MEMORY, sql(SQL_SELECT, null)));
        verifier.verifyTrace(event(SQLITE_EXECUTE_QUERY, executeQuery, null, "", MEMORY, sql(SQL_SELECT, null)));
    }

    @Test
    public void transaction() throws Exception {
        executeTransaction();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Class<?> driverClass = Class.forName("org.sqlite.JDBC");
        Method connect = driverClass.getDeclaredMethod("connect", String.class, Properties.class);
        verifier.verifyTrace(event(SQLITE, connect, null, "", MEMORY, cachedArgs(JDBC_PREFIX)));
        verifier.verifyTrace(event(SQLITE, connect, null, "", MEMORY, cachedArgs(JDBC_PREFIX)));

        Class<?> connectionClass = Class.forName("org.sqlite.jdbc4.JDBC4Connection");
        Method setAutoCommit = connectionClass.getMethod("setAutoCommit", boolean.class);
        Method commit = connectionClass.getMethod("commit");
        Method rollback = connectionClass.getMethod("rollback");
        verifier.verifyTrace(event(SQLITE, setAutoCommit, null, "", MEMORY, args(false)));

        Method prepareStatement = connectionClass.getDeclaredMethod("prepareStatement", String.class, int.class, int.class, int.class);
        verifier.verifyTrace(event(SQLITE, prepareStatement, null, "", MEMORY, sql(SQL_CREATE_TABLE, null)));

        Class<?> preparedStatementClass = Class.forName("org.sqlite.jdbc4.JDBC4PreparedStatement");
        Method execute = preparedStatementClass.getMethod("execute");
        Method executeUpdate = preparedStatementClass.getMethod("executeUpdate");
        Method executeQuery = preparedStatementClass.getMethod("executeQuery");

        verifier.verifyTrace(event(SQLITE_EXECUTE_QUERY, execute, null, "", MEMORY, sql(SQL_CREATE_TABLE, null)));
        verifier.verifyTrace(event(SQLITE, commit, null, "", MEMORY));
        verifier.verifyTrace(event(SQLITE, prepareStatement, null, "", MEMORY, sql(PSTMT_INSERT, null)),
            event(SQLITE_EXECUTE_QUERY, executeUpdate, null, "", MEMORY, sql(PSTMT_INSERT, null, new Object[]{1, "leo"} )));
        verifier.verifyTrace(event(SQLITE, rollback, null, "", MEMORY));
        verifier.verifyTrace(event(SQLITE_EXECUTE_QUERY, executeUpdate, null, "", MEMORY, sql(PSTMT_INSERT, null, new Object[]{2, "yui"} )));
        verifier.verifyTrace(event(SQLITE, commit, null, "", MEMORY));
        verifier.verifyTrace(event(SQLITE, prepareStatement, null, "", MEMORY, sql(SQL_SELECT, null)));
        verifier.verifyTrace(event(SQLITE_EXECUTE_QUERY, executeQuery, null, "", MEMORY, sql(SQL_SELECT, null)));
    }
}
