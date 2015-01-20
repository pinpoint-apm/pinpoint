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

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.navercorp.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.navercorp.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.navercorp.pinpoint.profiler.modifier.db.StringMaker;

/**
 *
 * @author Barney Kim
 */
public class SqlServerConnectionStringParser implements ConnectionStringParser {

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.modifier.db.ConnectionStringParser#parse(java.lang.String)
     */
    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return JDBCUrlParser.createUnknownDataBase(ServiceType.MSSQL, ServiceType.MSSQL_EXECUTE_QUERY, null);
        }

        StringMaker maker = new StringMaker(url);

        maker.lower().after("jdbc:sqlserver:");

        StringMaker before = maker.after("//").before(';');
        final String hostAndPortAndDataBaseString = before.value();
        String hostAndPortString = hostAndPortAndDataBaseString;

        List<String> hostList = new ArrayList<String>(1);
        hostList.add(hostAndPortString);
        String databaseId = maker.next().after("databasename=").before(';').value();

        String normalizedUrl = maker.clear().before(";").value();

        return new DefaultDatabaseInfo(ServiceType.MSSQL, ServiceType.MSSQL_EXECUTE_QUERY, url, normalizedUrl,
                hostList, databaseId);
    }

}
