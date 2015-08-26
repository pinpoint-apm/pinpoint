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

package com.navercorp.pinpoint.profiler.modifier.db.cubrid;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.navercorp.pinpoint.profiler.modifier.db.cubrid.CubridConnectionStringParser;

import org.junit.Assert;

import org.junit.Test;

/**
 * @author emeroad
 */
public class CubridConnectionStringParserTest {

    private final ConnectionStringParser parser = new CubridConnectionStringParser();
    @Test
    public void testParse() {
        String cubrid = "jdbc:cubrid:10.99.196.126:34001:nrdwapw:::?charset=utf-8:";

        DatabaseInfo dbInfo = parser.parse(cubrid);

        Assert.assertEquals(dbInfo.getType(), ServiceType.CUBRID);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.99.196.126:34001");
        Assert.assertEquals(dbInfo.getDatabaseId(), "nrdwapw");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:cubrid:10.99.196.126:34001:nrdwapw:::");
    }

    @Test
    public void testNullParse() {

        DatabaseInfo dbInfo = parser.parse(null);

        Assert.assertEquals(dbInfo.getType(), ServiceType.CUBRID);
        Assert.assertEquals(dbInfo.getHost().get(0), "error");
        Assert.assertEquals(dbInfo.getDatabaseId(), "error");
        Assert.assertEquals(dbInfo.getUrl(), null);

//        Assert.assertEquals(dbInfo.getUrl(), cubrid);
    }
}
