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

package com.navercorp.pinpoint.plugin.jdbc.mariadb;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParsingResult;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dawidmalina
 */
public class MariaDBJdbcUrlParser implements JdbcUrlParser {

    private static final String JDBC_URL_PREFIX = "jdbc:mariadb:";
    // jdbc:mariadb:loadbalance://10.22.33.44:3306,10.22.33.55:3306/MariaDB?characterEncoding=UTF-8
    private static final String JDBC_MARIADB_LOADBALANCE = JDBC_URL_PREFIX + "loadbalance:";

    @Override
    public JdbcUrlParsingResult parse(String url) {
        if (url == null) {
            return new JdbcUrlParsingResult(false, UnKnownDatabaseInfo.createUnknownDataBase(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, null));
        }

        if (isLoadbalanceUrl(url)) {
            return parseLoadbalancedUrl(url);
        }
        return parseNormal(url);
    }

    private JdbcUrlParsingResult parseLoadbalancedUrl(String url) {
        // jdbc:mariadb://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(JDBC_URL_PREFIX);
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();

        // Decided not to cache regex. This is not invoked often so don't waste
        // memory.
        String[] parsedHost = host.split(",");
        List<String> hostList = Arrays.asList(parsedHost);

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();

        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
        return new JdbcUrlParsingResult(databaseInfo);
    }

    private boolean isLoadbalanceUrl(String url) {
        return url.regionMatches(true, 0, JDBC_MARIADB_LOADBALANCE, 0, JDBC_MARIADB_LOADBALANCE.length());
    }

    private JdbcUrlParsingResult parseNormal(String url) {
        // jdbc:mariadb://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(JDBC_URL_PREFIX);
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
        // String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
        return new JdbcUrlParsingResult(databaseInfo);
    }

    @Override
    public ServiceType getServiceType() {
        return MariaDBConstants.MARIADB;
    }

    @Override
    public boolean isPrefixMatch(String url) {
        if (url == null) {
            return false;
        }

        return url.startsWith(JDBC_URL_PREFIX);
    }

}
