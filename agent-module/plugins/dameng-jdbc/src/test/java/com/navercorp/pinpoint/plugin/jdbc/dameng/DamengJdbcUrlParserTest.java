/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.dameng;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author yjqg6666
 */
public class DamengJdbcUrlParserTest {

    private final DamengJdbcUrlParser jdbcUrlParser = new DamengJdbcUrlParser();

    private static final ServiceType SERVICE_TYPE = DamengConstants.DAMENG;
    private static final String I_SEP = ":";
    private static final String H_SEP = ",";
    private static final String IP = "1.2.3.4";
    private static final String PORT = "5236";
    private static final String IP_PORT = IP + I_SEP + PORT;

    private static final String IP2 = "5.6.7.8";
    private static final String PORT2 = "5000";
    private static final String IP_PORT2 = IP2 + I_SEP + PORT2;

    private static final String GRP = "GRP";

    private static final String PREFIX = DamengJdbcUrlParser.JDBC_PREFIX;
    private static final String CONNECTION_STRING = PREFIX + IP_PORT;
    private static final String CONNECTION_STRING_NO_PORT = PREFIX + IP;

    @Test
    public void normal() {
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000");
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), (IP_PORT));
        Assertions.assertEquals(dbInfo.getDatabaseId(), IP_PORT);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
    }

    @Test
    public void noProp() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), (IP_PORT));
        Assertions.assertEquals(dbInfo.getDatabaseId(), IP_PORT);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
    }

    @Test
    public void ipOnly() {
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000");
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING_NO_PORT + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), (IP_PORT));
        Assertions.assertEquals(dbInfo.getDatabaseId(), IP_PORT);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
    }

    @Test
    public void ipOnlyNoProp() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING_NO_PORT);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), (IP_PORT));
        Assertions.assertEquals(dbInfo.getDatabaseId(), IP_PORT);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
    }

    @Test
    public void hostProp() {
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000", "host=" + IP2);
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        String ipPort = IP2 + I_SEP + PORT;
        Assertions.assertEquals(dbInfo.getHost().get(0), (ipPort));
        Assertions.assertEquals(dbInfo.getDatabaseId(), ipPort);
        Assertions.assertEquals(dbInfo.getUrl(), PREFIX + ipPort);
    }

    @Test
    public void portProp() {
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000", "port=" + PORT2);
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        String ipPort = IP + I_SEP + PORT2;
        Assertions.assertEquals(dbInfo.getHost().get(0), (ipPort));
        Assertions.assertEquals(dbInfo.getDatabaseId(), ipPort);
        Assertions.assertEquals(dbInfo.getUrl(), PREFIX + ipPort);
    }

    @Test
    public void hostPortProp() {
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000", "host=" + IP2, "port=" + PORT2);
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        String ipPort = IP2 + I_SEP + PORT2;
        Assertions.assertEquals(dbInfo.getHost().get(0), (ipPort));
        Assertions.assertEquals(dbInfo.getDatabaseId(), ipPort);
        Assertions.assertEquals(dbInfo.getUrl(), PREFIX + ipPort);
    }

    @Test
    public void propNoValue() {
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000", "enableSth", "");
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), (IP_PORT));
        Assertions.assertEquals(dbInfo.getDatabaseId(), IP_PORT);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
    }

    @Test
    public void groupSingleHost() {
        String jdbc = PREFIX + GRP;
        String ipPort = IP_PORT;
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000", GRP + "=(" + ipPort + ")");
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbc + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getDatabaseId(), IP_PORT);
        Assertions.assertEquals(dbInfo.getUrl(), PREFIX + ipPort);
    }

    @Test
    public void groupMultiHost() {
        String jdbc = PREFIX + GRP;
        String ipPort = IP_PORT + H_SEP + IP_PORT2;
        String prop = String.join("&amp;", "compatibleMode=mysql", "connectTimeout=3000", GRP + "=(" + ipPort + ")");
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbc + "?" + prop);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getHost().get(1), IP_PORT2);
        Assertions.assertEquals(dbInfo.getDatabaseId(), ipPort);
        Assertions.assertEquals(dbInfo.getUrl(), PREFIX + ipPort);
    }

    @Test
    public void failNull() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(null);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void failInvalid() {
        final String jdbcUrl = "jdbc:anything://1.2.3.4:3306";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void failNotJdbc() {
        final String jdbcUrl = "anything://1.2.3.4:3306";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

}
