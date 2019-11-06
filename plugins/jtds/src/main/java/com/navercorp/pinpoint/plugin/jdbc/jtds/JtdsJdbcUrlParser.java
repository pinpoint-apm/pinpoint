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

package com.navercorp.pinpoint.plugin.jdbc.jtds;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class JtdsJdbcUrlParser implements JdbcUrlParserV2 {

    public static final int DEFAULT_PORT = 1433;

    private static final String URL_PREFIX = "jdbc:jtds:sqlserver:";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl must not be null");
            return UnKnownDatabaseInfo.INSTANCE;
        }
        if (!jdbcUrl.startsWith(URL_PREFIX)) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, prefix:{})", jdbcUrl, URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo result = null;
        try {
            result = parse0(jdbcUrl);
        } catch (Exception e) {
            logger.info("JtdsJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(JtdsConstants.MSSQL, JtdsConstants.MSSQL_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String url) {
//        jdbc:jtds:sqlserver://10.xx.xx.xx:1433;DatabaseName=CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        StringMaker maker = new StringMaker(url);

        maker.lower().after(URL_PREFIX);

        StringMaker before = maker.after("//").before(';');
        final String hostAndPortAndDataBaseString = before.value();
        String databaseId = "";
        String hostAndPortString = "";
        final int databaseIdIndex = hostAndPortAndDataBaseString.indexOf('/');
        if (databaseIdIndex != -1) {
            hostAndPortString = hostAndPortAndDataBaseString.substring(0, databaseIdIndex);
            databaseId = hostAndPortAndDataBaseString.substring(databaseIdIndex + 1, hostAndPortAndDataBaseString.length());
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

        return new DefaultDatabaseInfo(JtdsConstants.MSSQL, JtdsConstants.MSSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return JtdsConstants.MSSQL;
    }

}
