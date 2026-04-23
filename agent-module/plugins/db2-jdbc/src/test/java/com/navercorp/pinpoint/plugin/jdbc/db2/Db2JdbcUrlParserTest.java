/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.db2;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataRegistrar;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Db2JdbcUrlParserTest {

    private static final short DB2_CODE = 2160;
    private static final String DB2_NAME = "DB2";
    private static final short DB2_EXECUTE_QUERY_CODE = 2161;
    private static final String DB2_EXECUTE_QUERY_NAME = "DB2_EXECUTE_QUERY";

    private final Db2JdbcUrlParser jdbcUrlParser = new Db2JdbcUrlParser();

    @BeforeAll
    public static void beforeClass() {
        ServiceTypeLocator locator = mock(ServiceTypeLocator.class);
        ServiceType db2 = newServiceType(DB2_CODE, DB2_NAME);
        ServiceType db2ExecuteQuery = newServiceType(DB2_EXECUTE_QUERY_CODE, DB2_EXECUTE_QUERY_NAME);
        when(locator.findServiceTypeByName(DB2_NAME)).thenReturn(db2);
        when(locator.findServiceTypeByName(DB2_EXECUTE_QUERY_NAME)).thenReturn(db2ExecuteQuery);
        when(locator.findServiceType(DB2_CODE)).thenReturn(db2);
        when(locator.findServiceType(DB2_EXECUTE_QUERY_CODE)).thenReturn(db2ExecuteQuery);
        TraceMetadataRegistrar.registerServiceTypes(locator);
    }

    private static ServiceType newServiceType(short code, String name) {
        ServiceType serviceType = mock(ServiceType.class);
        when(serviceType.getCode()).thenReturn(code);
        when(serviceType.getName()).thenReturn(name);
        return serviceType;
    }

    @Test
    public void remoteUrl_hostPortDatabase() {
        String url = "jdbc:db2://host1:50000/sample";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals(Db2Constants.DB2, info.getType());
        Assertions.assertEquals(1, info.getHost().size());
        Assertions.assertEquals("host1:50000", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
        Assertions.assertEquals(url, info.getUrl());
        Assertions.assertEquals(url, info.getRealUrl());
    }

    @Test
    public void remoteUrl_hostOnly() {
        String url = "jdbc:db2://host1/sample";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals("host1", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
    }

    @Test
    public void remoteUrl_withColonProperties() {
        // "jdbc:db2://host:port/db:prop=val;prop=val;" — the "extended" JCC option syntax
        String url = "jdbc:db2://db2srv:50000/sample:currentSchema=APP;retrieveMessagesFromServerOnGetMessage=true;";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals("db2srv:50000", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
        Assertions.assertEquals("jdbc:db2://db2srv:50000/sample", info.getUrl());
        Assertions.assertEquals(url, info.getRealUrl());
    }

    @Test
    public void remoteUrl_withSemicolonProperties() {
        String url = "jdbc:db2://db2srv:50000/sample;user=appuser;password=secret";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals("db2srv:50000", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
        Assertions.assertEquals("jdbc:db2://db2srv:50000/sample", info.getUrl());
    }

    @Test
    public void remoteUrl_withQuestionMarkProperties() {
        String url = "jdbc:db2://db2srv:50000/sample?user=appuser&password=secret";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals("db2srv:50000", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
        Assertions.assertEquals("jdbc:db2://db2srv:50000/sample", info.getUrl());
    }

    @Test
    public void remoteUrl_ipv4Host() {
        String url = "jdbc:db2://10.0.0.5:60000/prod";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals("10.0.0.5:60000", info.getHost().get(0));
        Assertions.assertEquals("prod", info.getDatabaseId());
    }

    @Test
    public void localUrl_type2() {
        // Type 2 (native/local) driver: jdbc:db2:<database-alias>
        String url = "jdbc:db2:sample";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals(Db2Constants.DB2, info.getType());
        Assertions.assertEquals("localhost", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
        Assertions.assertEquals("jdbc:db2:sample", info.getUrl());
        Assertions.assertEquals(url, info.getRealUrl());
    }

    @Test
    public void localUrl_type2_withProperties() {
        String url = "jdbc:db2:sample:currentSchema=APP";
        DatabaseInfo info = jdbcUrlParser.parse(url);

        Assertions.assertTrue(info.isParsingComplete());
        Assertions.assertEquals("localhost", info.getHost().get(0));
        Assertions.assertEquals("sample", info.getDatabaseId());
        Assertions.assertEquals("jdbc:db2:sample", info.getUrl());
    }

    @Test
    public void parse_null_returnsUnknown() {
        DatabaseInfo info = jdbcUrlParser.parse(null);

        Assertions.assertFalse(info.isParsingComplete());
        Assertions.assertEquals(ServiceType.UNKNOWN_DB, info.getType());
    }

    @Test
    public void parse_nonDb2Prefix_returnsUnknown() {
        DatabaseInfo info = jdbcUrlParser.parse("jdbc:mysql://host1:3306/sample");

        Assertions.assertFalse(info.isParsingComplete());
        Assertions.assertEquals(ServiceType.UNKNOWN_DB, info.getType());
    }

    @Test
    public void parse_emptyString_returnsUnknown() {
        DatabaseInfo info = jdbcUrlParser.parse("");

        Assertions.assertFalse(info.isParsingComplete());
        Assertions.assertEquals(ServiceType.UNKNOWN_DB, info.getType());
    }

    @Test
    public void getServiceType_returnsDb2() {
        Assertions.assertEquals(Db2Constants.DB2, jdbcUrlParser.getServiceType());
    }
}
