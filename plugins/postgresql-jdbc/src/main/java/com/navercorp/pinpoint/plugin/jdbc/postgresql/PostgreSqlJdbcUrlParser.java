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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Brad Hong
 */
public class PostgreSqlJdbcUrlParser implements JdbcUrlParserV2 {

    private static final String URL_PREFIX = "jdbc:postgresql:";
    // jdbc:postgresql:loadbalance://10.22.33.44:3306,10.22.33.55:3306/PostgreSQL?characterEncoding=UTF-8
    private static final String LOADBALANCE_URL_PREFIX = URL_PREFIX + "loadbalance:";

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
            logger.info("PostgreJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(PostgreSqlConstants.POSTGRESQL, PostgreSqlConstants.POSTGRESQL_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String url) {
        if (isLoadbalanceUrl(url)) {
            return parseLoadbalancedUrl(url);
        }
        return parseNormal(url);
    }

    private DatabaseInfo parseLoadbalancedUrl(String url) {
        // jdbc:postgresql://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(URL_PREFIX);
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();

        // Decided not to cache regex. This is not invoked often so don't waste memory.
        String[] parsedHost = host.split(",");
        List<String> hostList = Arrays.asList(parsedHost);


        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();

        return new DefaultDatabaseInfo(PostgreSqlConstants.POSTGRESQL, PostgreSqlConstants.POSTGRESQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }

    private boolean isLoadbalanceUrl(String url) {
        return url.regionMatches(true, 0, LOADBALANCE_URL_PREFIX, 0, LOADBALANCE_URL_PREFIX.length());
    }

    private DatabaseInfo parseNormal(String url) {
     // jdbc:postgresql://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(URL_PREFIX);
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
        // String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(PostgreSqlConstants.POSTGRESQL, PostgreSqlConstants.POSTGRESQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return PostgreSqlConstants.POSTGRESQL;
    }

}
