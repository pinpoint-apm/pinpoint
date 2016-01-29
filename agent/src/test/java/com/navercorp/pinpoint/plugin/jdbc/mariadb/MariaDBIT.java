package com.navercorp.pinpoint.plugin.mariadb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;

import ch.vorburger.mariadb4j.DB;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent
@Dependency({ "org.mariadb.jdbc:mariadb-java-client:1.3.3", "ch.vorburger.mariaDB4j:mariaDB4j:2.1.3" })
@JvmVersion(7)
@Repository("http://jcenter.bintray.com")
public class MariaDBTest {

    DB db;

    @Before
    public void init() throws Exception {
        db = DB.newEmbeddedDB(13306);
        db.start();
        db.createDB("test");
        db.source("mariadb/init.sql");
    }

    @Test
    public void testConnection() throws Exception {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        Connection connection = DriverManager.getConnection("jdbc:mariadb://127.0.0.1:13306/test", "root", null);

        verifier.verifyTraceCount(1);

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT count(1) FROM playground");
        result.first();
        assertEquals(3, result.getInt(1));

        verifier.verifyTraceCount(2);

        PreparedStatement ps = connection.prepareStatement("SELECT * FROM playground where id = ?");
        ps.setInt(1, 3);
        result = ps.executeQuery();
        result.first();
        assertEquals("THREE", result.getString(2));

        verifier.verifyTraceCount(4);
        verifier.printCache();
    }

    @After
    public void cleanUp() throws Exception {
        db.stop();
    }

}
