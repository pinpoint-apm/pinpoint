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

package com.navercorp.pinpoint.plugin.jdbc.informix;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class InformixConnectionStringParserTest {

    private final JdbcUrlParserV2 parser = new InformixJdbcUrlParser();
    
    @Test
    public void testParse1() {
        String informix = "jdbc:informix-sqli:10.99.196.126:11000/database_name:INFORMIXSERVER=server_name";
        DatabaseInfo dbInfo = parser.parse(informix);
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(InformixConstants.INFORMIX, dbInfo.getType());
        Assert.assertEquals("10.99.196.126:11000", dbInfo.getHost().get(0));
        Assert.assertEquals("database_name", dbInfo.getDatabaseId());
        Assert.assertEquals(informix, dbInfo.getUrl());
    }

    @Test
    public void parseFailTest1() {
        DatabaseInfo dbInfo = parser.parse(null);
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void parseFailTest2() {
        String informix = "jdbc:mysql:10.99.196.126:11000/database_name:INFORMIXSERVER=server_name";
        DatabaseInfo dbInfo = parser.parse(informix);
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

}
