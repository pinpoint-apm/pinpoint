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
package com.navercorp.pinpoint.profiler.modifier.db.mssql.sqlserver;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.db.mssql.sqlserver.SqlServerConnectionStringParser;

/**
 *
 * @author Barney Kim
 */
public class SqlServerConnectionStringParserTest {

    @Test
    public void parse() throws Exception {
        String url = "jdbc:sqlserver://192.168.0.2:1433;databasename=testdb;selectMethod=cursor";

        SqlServerConnectionStringParser parser = new SqlServerConnectionStringParser();
        DatabaseInfo info = parser.parse(url);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getExecuteQueryType(), ServiceType.MSSQL_EXECUTE_QUERY);
        Assert.assertEquals(info.getMultipleHost(), "192.168.0.2:1433");
        Assert.assertEquals(info.getDatabaseId(), "testdb");
        Assert.assertEquals(info.getUrl(), "jdbc:sqlserver://192.168.0.2:1433");
    }

}
