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

package com.navercorp.pinpoint.profiler.modifier.db.mssql.jtds;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.navercorp.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.navercorp.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.navercorp.pinpoint.profiler.modifier.db.StringMaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class JtdsConnectionStringParser implements ConnectionStringParser {

    public static final int DEFAULT_PORT = 1433;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return JDBCUrlParser.createUnknownDataBase(ServiceType.MSSQL, ServiceType.MSSQL_EXECUTE_QUERY, null);
        }

//        jdbc:jtds:sqlserver://10.xx.xx.xx:1433;DatabaseName=CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        StringMaker maker = new StringMaker(url);

        maker.lower().after("jdbc:jtds:sqlserver:");

        StringMaker before = maker.after("//").before(';');
        final String hostAndPortAndDataBaseString = before.value();
        String databaseId = "";
        String hostAndPortString = "";
        final int databaseIdIndex = hostAndPortAndDataBaseString.indexOf('/');
        if (databaseIdIndex != -1) {
            hostAndPortString = hostAndPortAndDataBaseString.substring(0, databaseIdIndex);
            databaseId = hostAndPortAndDataBaseString.substring(databaseIdIndex+1, hostAndPortAndDataBaseString.length());
        } else {
            hostAndPortString = hostAndPortAndDataBaseString;
        }

        List<String> hostList = new ArrayList<String>(1);
        hostList.add(hostAndPortString);
        // option properties search
        if (databaseId.isEmpty()) {
            databaseId = maker.next().after("databasename=").before(';').value();
        }

        String normalizedUrl = maker.clear().before(";").value();

        return new DefaultDatabaseInfo(ServiceType.MSSQL, ServiceType.MSSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }


}
