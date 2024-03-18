/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.jdbc.clickhouse;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlNormalizer;
import com.navercorp.pinpoint.common.profiler.sql.NormalizedSql;
import com.navercorp.pinpoint.common.profiler.sql.SqlNormalizer;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.template.DriverManagerDataSource;
import com.navercorp.pinpoint.plugin.jdbc.clickhouse.ClickHouseConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

/**
 * @author intr3p1d
 */
public class ClickHouseITBase {
    private final Logger logger = LogManager.getLogger(getClass());
    private static final String TABLE_NAME = "jdbc_example_basic";

    private static final SqlNormalizer sqlNormalizer = new DefaultSqlNormalizer();

    protected DriverProperties driverProperties;
    protected ClickHouseITHelper clickHouseITHelper;

    protected String DB_TYPE = ClickHouseConstants.CLICK_HOUSE.getName();
    protected String DB_EXECUTE_QUERY = ClickHouseConstants.CLICK_HOUSE_EXECUTE_QUERY.getName();

    protected String jdbcUrl;
    protected String databaseId;
    protected String databaseIdPassword;
    protected String databaseAddress;
    protected String databaseName;
    protected JDBCDriverClass jdbcDriverClass;
    protected JDBCApi jdbcApi;
    private DataSource dataSource;

    public void setup(
            DriverProperties driverProperties,
            JdbcUrlParserV2 jdbcUrlParser,
            JDBCDriverClass jdbcDriverClass,
            JDBCApi jdbcApi
    ) {
        this.driverProperties = driverProperties;
        this.clickHouseITHelper = new ClickHouseITHelper(driverProperties);

        this.jdbcUrl = driverProperties.getUrl();

        DatabaseInfo databaseInfo = jdbcUrlParser.parse(jdbcUrl);

        this.databaseAddress = databaseInfo.getHost().get(0);
        this.databaseName = databaseInfo.getDatabaseId();

        this.databaseId = driverProperties.getUser();
        this.databaseIdPassword = driverProperties.getPassword();
        this.dataSource = new DriverManagerDataSource(jdbcUrl, databaseId, databaseIdPassword);

        this.jdbcDriverClass = jdbcDriverClass;
        this.jdbcApi = jdbcApi;

        try {
            Driver driver = jdbcDriverClass.getDriver().newInstance();
            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            throw new RuntimeException("driver register error", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return clickHouseITHelper.getConnection();
    }

    public void testStatements() throws SQLException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        Connection conn = getConnection();

        String sql1 = String.format(
                "drop table if exists %1$s; create table %1$s(a String, b Nullable(String)) engine=Memory",
                TABLE_NAME);
        String sql2 = "select * from " + TABLE_NAME;
        String sql3 = "select * from " + TABLE_NAME;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql1);
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.setMaxRows(3);
            int count = 0;
            try (ResultSet rs = stmt.executeQuery(sql2)) {
                while (rs.next()) {
                    count++;
                }
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.setMaxRows(3);
            int count = stmt.executeUpdate(sql3);
        }

        verifier.printCache();

        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(Expectations.event(DB_TYPE, connect, null, databaseAddress, databaseName, Expectations.cachedArgs(jdbcUrl)));

        Method execute = jdbcApi.getStatement().getExecute();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(sql1, null)));

        Method executeQuery = jdbcApi.getStatement().getExecuteQuery();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executeQuery, null, databaseAddress, databaseName, Expectations.sql(sql2, null)));

        Method executeUpdate = jdbcApi.getStatement().getExecuteUpdate();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executeUpdate, null, databaseAddress, databaseName, Expectations.sql(sql3, null)));

    }

    public void testPreparedStatements() throws SQLException {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        Connection conn = getConnection();

        String sql0 = "INSERT INTO test (name, age) VALUES (?, 5)";

        String sql1 = "drop table if exists t_map;"
                + "CREATE TABLE t_map"
                + "("
                + "    `audit_seq` Int64 CODEC(Delta(8), LZ4),"
                + "`timestamp` Int64 CODEC(Delta(8), LZ4),"
                + "`event_type` LowCardinality(String),"
                + "`event_subtype` LowCardinality(String),"
                + "`actor_type` LowCardinality(String),"
                + "`actor_id` String,"
                + "`actor_tenant_id` LowCardinality(String),"
                + "`actor_tenant_name` String,"
                + "`actor_firstname` String,"
                + "`actor_lastname` String,"
                + "`resource_type` LowCardinality(String),"
                + "`resource_id` String,"
                + "`resource_container` LowCardinality(String),"
                + "`resource_path` String,"
                + "`origin_ip` String,"
                + "`origin_app_name` LowCardinality(String),"
                + "`origin_app_instance` String,"
                + "`description` String,"
                + "`attributes` Map(String, String)"
                + ")"
                + "ENGINE = MergeTree "
                + "ORDER BY (resource_container, event_type, event_subtype) "
                + "SETTINGS index_granularity = 8192";
        String sql2 = "INSERT INTO t_map SETTINGS async_insert=1,wait_for_async_insert=1 " +
                "VALUES (8481365034795008,1673349039830,'operation-9','a','service', 'bc3e47b8-2b34-4c1a-9004-123656fa0000','b', 'c', 'service-56','d', 'object','e', 'my-value-62', 'mypath', 'some.hostname.address.com', 'app-9', 'instance-6','x', ?)";


        try (PreparedStatement stmt = conn.prepareStatement(sql0)) {
            stmt.setString(1, "maru");
            stmt.execute();
        }

        try (Statement s = conn.createStatement()) {
            s.execute(sql1);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.setObject(1, Collections.singletonMap("key1", "value1"));
            stmt.execute();
        }

        verifier.printCache();

        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(Expectations.event(DB_TYPE, connect, null, databaseAddress, databaseName, Expectations.cachedArgs(jdbcUrl)));

        NormalizedSql normalizedSql0 = normalize(sql0);

        Method prepare1 = jdbcApi.getConnection().getPrepareStatement();
        verifier.verifyTrace(Expectations.event(DB_TYPE, prepare1, null, databaseAddress, databaseName,
                Expectations.sql(normalizedSql0.getNormalizedSql(), normalizedSql0.getParseParameter())));

        Method executePrepared1 = jdbcApi.getPreparedStatement().getExecute();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executePrepared1, null, databaseAddress, databaseName,
                Expectations.sql(normalizedSql0.getNormalizedSql(), normalizedSql0.getParseParameter(), "maru")));


        NormalizedSql normalizedSql1 = normalize(sql1);
        Method execute = jdbcApi.getStatement().getExecute();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, execute, null, databaseAddress, databaseName,
                Expectations.sql(normalizedSql1.getNormalizedSql(), normalizedSql1.getParseParameter())));

        NormalizedSql normalizedSql2 = normalize(sql2);
        Method prepare2 = jdbcApi.getConnection().getPrepareStatement();
        verifier.verifyTrace(Expectations.event(DB_TYPE, prepare2, null, databaseAddress, databaseName,
                Expectations.sql(normalizedSql2.getNormalizedSql(), normalizedSql2.getParseParameter())));

        Method executePrepared2 = jdbcApi.getPreparedStatement().getExecute();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executePrepared2, null, databaseAddress, databaseName,
                Expectations.sql(normalizedSql2.getNormalizedSql(), normalizedSql2.getParseParameter(), "java.util.Collections$SingletonMap")));
    }

    private NormalizedSql normalize(String sql) {
        return sqlNormalizer.normalizeSql(sql);
    }

}
