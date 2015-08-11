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

package com.navercorp.pinpoint.plugin.jdbc.cubrid;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.plugin.jdbc.cubrid.CubridConstants;
import com.navercorp.pinpoint.plugin.jdbc.cubrid.CubridJdbcUrlParser;

/**
 * @author emeroad
 */
public class CubridConnectionStringParserTest {

    private final JdbcUrlParser parser = new CubridJdbcUrlParser();
    
    @Test
    public void testParse() {
        String cubrid = "jdbc:cubrid:10.99.196.126:34001:nrdwapw:::?charset=utf-8:";

        DatabaseInfo dbInfo = parser.parse(cubrid);

        Assert.assertEquals(CubridConstants.CUBRID, dbInfo.getType());
        Assert.assertEquals("10.99.196.126:34001", dbInfo.getHost().get(0));
        Assert.assertEquals("nrdwapw", dbInfo.getDatabaseId());
        Assert.assertEquals("jdbc:cubrid:10.99.196.126:34001:nrdwapw:::", dbInfo.getUrl());
    }

    @Test
    public void testNullParse() {
        DatabaseInfo dbInfo = parser.parse(null);
        Assert.assertEquals(UnKnownDatabaseInfo.INSTANCE, dbInfo);
    }
}
