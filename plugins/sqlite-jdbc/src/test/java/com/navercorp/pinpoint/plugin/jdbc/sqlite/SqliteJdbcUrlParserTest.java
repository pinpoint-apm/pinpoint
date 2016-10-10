package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;

public class SqliteJdbcUrlParserTest {

    private SqliteJdbcUrlParser parser;

    @Before
    public void setup() throws Exception {
        parser = new SqliteJdbcUrlParser();
    }

    @Test
    public void memory() throws Exception {
        String url = "jdbc:sqlite:";

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, SqliteJdbcUrlParser.MEMORY, null, databaseInfo);
    }

    @Test
    public void memory1() throws Exception {
        String url = "jdbc:sqlite:" + SqliteJdbcUrlParser.MEMORY;

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, SqliteJdbcUrlParser.MEMORY, null, databaseInfo);
    }

    @Test
    public void file() throws Exception {
        String databaseId = "foo.sqlite3";
        String url = "jdbc:sqlite:" + databaseId;

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, databaseId, null, databaseInfo);
    }

    @Test
    public void fileWithPath() throws Exception {
        String databaseId = "foo.sqlite3";
        String path = "/home/foo/";
        String url = "jdbc:sqlite:" + path + databaseId;

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, databaseId, null, databaseInfo);
    }

    @Test
    public void resource() throws Exception {
        String databaseId = "foo.sqlite3";
        String path = "org/foo/";
        String url = "jdbc:sqlite:" + path + databaseId;

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, databaseId, null, databaseInfo);
    }

    @Test
    public void externalResource() throws Exception {
        String databaseId = "foo.sqlite3";
        String path = "https://foo.com/foo/";
        String url = "jdbc:sqlite:" + SqliteJdbcUrlParser.RESOURCE + path + databaseId;

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, databaseId, "foo.com", databaseInfo);
    }

    @Test
    public void externalJarResource() throws Exception {
        String databaseId = "foo.sqlite3";
        String path = "jar:https://foo.com/foo/bar.jar!/org/foo/";
        String url = "jdbc:sqlite:" + SqliteJdbcUrlParser.RESOURCE + path + databaseId;

        DatabaseInfo databaseInfo = parser.doParse(url);

        assertDbInfo(url, databaseId, "foo.com", databaseInfo);
    }

    private void assertDbInfo(String url, String databaseId, String host, DatabaseInfo databaseInfo) {
        assertThat(databaseInfo.getType(), is(SqlitePluginConstants.SQLITE));
        assertThat(databaseInfo.getExecuteQueryType(), is(SqlitePluginConstants.SQLITE_EXECUTE_QUERY));
        if(host == null) {
            assertThat(databaseInfo.getHost().size(), is(0));
        } else {
            assertThat(databaseInfo.getHost().size(), is(1));
            assertThat(databaseInfo.getHost().get(0), is(host));
        }
        assertThat(databaseInfo.getDatabaseId(), is(databaseId));
        assertThat(databaseInfo.getUrl(), is(url));
    }
}
