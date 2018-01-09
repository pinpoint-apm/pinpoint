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
 * @author dawidmalina
 */
public class MariaDBJdbcUrlParser implements JdbcUrlParserV2 {

//    jdbc:(mysql|mariadb):[replication:|failover|loadbalance:|aurora:]//<hostDescription>[,<hostDescription>]/[database>]
//    jdbc:mariadb:loadbalance://10.22.33.44:3306,10.22.33.55:3306/MariaDB?characterEncoding=UTF-8
    private static final String URL_PREFIX = "jdbc:mariadb:";

    private static final String MYSQL_URL_PREFIX = "jdbc:mysql:";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl must not be null");
            return UnKnownDatabaseInfo.INSTANCE;
        }

        Type type = Type.findType(jdbcUrl);
        if (type == null) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, prefix:{}, {})", jdbcUrl, URL_PREFIX, MYSQL_URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo result = null;
        try {
            result = parse0(jdbcUrl, type);
        } catch (Exception e) {
            logger.info("MaridDBJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String url, Type type) {
        if (isLoadbalanceUrl(url, type)) {
            return parseLoadbalancedUrl(url, type);
        }
        return parseNormal(url, type);
    }

    private boolean isLoadbalanceUrl(String url, Type type) {
        String loadbalanceUrlPrefix = type.getLoadbalanceUrlPrefix();
        return url.regionMatches(true, 0, loadbalanceUrlPrefix, 0, loadbalanceUrlPrefix.length());
    }

    private DatabaseInfo parseLoadbalancedUrl(String url, Type type) {
        // jdbc:mariadb://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(type.getUrlPrefix());
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();

        // Decided not to cache regex. This is not invoked often so don't waste
        // memory.
        String[] parsedHost = host.split(",");
        List<String> hostList = Arrays.asList(parsedHost);

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();

        return new DefaultDatabaseInfo(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, url,
                normalizedUrl, hostList, databaseId);
    }

    private DatabaseInfo parseNormal(String url, Type type) {
        // jdbc:mariadb://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(type.getUrlPrefix());
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
        // String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, url,
                normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return MariaDBConstants.MARIADB;
    }

    private static enum Type {
        MARIA(URL_PREFIX),
        MYSQL(MYSQL_URL_PREFIX);

        private final String urlPrefix;
        private final String loadbalanceUrlPrefix;

        Type(String urlPrefix) {
            this.urlPrefix = urlPrefix;
            this.loadbalanceUrlPrefix = urlPrefix + "loadbalance:";
        }

        private String getUrlPrefix() {
            return urlPrefix;
        }

        private String getLoadbalanceUrlPrefix() {
            return urlPrefix + "loadbalance:";
        }

        private static Type findType(String jdbcUrl) {
            for (Type type : Type.values()) {
                if (jdbcUrl.startsWith(type.urlPrefix)) {
                    return type;
                }
            }

            return null;
        }

    }

}
