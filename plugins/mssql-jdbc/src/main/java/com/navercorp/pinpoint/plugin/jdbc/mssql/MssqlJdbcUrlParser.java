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

package com.navercorp.pinpoint.plugin.jdbc.mssql;

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
 * @author Harris Gwag ( gwagdalf )
 */
public class MssqlJdbcUrlParser implements JdbcUrlParserV2 {

    //    jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
    private static final String MSSQL_URL_PREFIX = "jdbc:sqlserver:";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl");
            return UnKnownDatabaseInfo.INSTANCE;
        }
        if (!jdbcUrl.startsWith(MSSQL_URL_PREFIX)) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, prefix:{})", jdbcUrl, MSSQL_URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }


        try {
            return parse0(jdbcUrl);
        } catch (Exception e) {
            logger.info("MssqlJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            return UnKnownDatabaseInfo
                    .createUnknownDataBase(MssqlConstants.MSSQL_JDBC, MssqlConstants.MSSQL_JDBC_QUERY,
                            jdbcUrl);
        }
    }


    private DatabaseInfo parse0(String url) {
        // jdbc:sqlserver://localhost;databaseName=AdventureWorks;integratedSecurity=true;applicationName=MyApp;
        StringMaker maker = new StringMaker(url);

        maker.after(MSSQL_URL_PREFIX);

        String host = maker.after("//").before(';').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
        // String port = maker.next().after(':').before(';').value();

        String databaseId = maker.next().after("databaseName=").before(';').value();
        String normalizedUrl =
                maker.clear().before("databaseName=").value() + "databaseName=" + databaseId;
        return new DefaultDatabaseInfo(MssqlConstants.MSSQL_JDBC, MssqlConstants.MSSQL_JDBC_QUERY, url,
                normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return MssqlConstants.MSSQL_JDBC;
    }

}
