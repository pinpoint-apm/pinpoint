package com.navercorp.pinpoint.plugin.jdbc.mariadb;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mariadb.jdbc.Driver;
import org.mariadb.jdbc.MariaDbConnection;
import org.mariadb.jdbc.MariaDbServerPreparedStatement;
import org.mariadb.jdbc.MariaDbStatement;

import ch.vorburger.mariadb4j.DB;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;

@RunWith(PinpointPluginTestSuite.class)
@JvmVersion(7)
@Repository("http://jcenter.bintray.com")
@Dependency({ "org.mariadb.jdbc:mariadb-java-client:[1.3.4,)", "ch.vorburger.mariaDB4j:mariaDB4j:2.1.3" })
public class MariaDBIT {

    private static final String JDBC_URL = "jdbc:mariadb://127.0.0.1:13306/test";
    private static final String PREPARED_STATEMENT = "SELECT * FROM playground where id = ?";

    private DB db;

    @Before
    public void init() throws Exception {
        db = DB.newEmbeddedDB(13306);
        db.start();
        db.createDB("test");
        db.source("jdbc/mariadb/init.sql");
    }

    @Test
    public void testConnection() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        Connection connection = DriverManager.getConnection(JDBC_URL, "root", null);

//        verifier.verifyTraceCount(1);

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT count(1) FROM playground");
        result.first();
        assertEquals(3, result.getInt(1));

        verifier.printCache();
        
        verifier.verifyTraceCount(2);
        verifier.verifyTrace(event("MARIADB", Driver.class.getMethod("connect", String.class, Properties.class), cachedArgs(JDBC_URL)));
        verifier.verifyTrace(event("MARIADB_EXECUTE_QUERY", MariaDbStatement.class.getMethod("executeQuery", String.class), sql("SELECT count(0#) FROM playground", "1")));

        PreparedStatement ps = connection.prepareStatement(PREPARED_STATEMENT);
        ps.setInt(1, 3);
        result = ps.executeQuery();
        result.first();
        assertEquals("THREE", result.getString(2));

        verifier.printCache();
        verifier.verifyTraceCount(2);
        verifier.verifyTrace(event("MARIADB", MariaDbConnection.class.getMethod("prepareStatement", String.class), sql(PREPARED_STATEMENT, null)));
        verifier.verifyTrace(event("MARIADB_EXECUTE_QUERY", MariaDbServerPreparedStatement.class.getMethod("executeQuery"), sql(PREPARED_STATEMENT, null, "3")));
    }

    @After
    public void cleanUp() throws Exception {
        db.stop();
    }

}
