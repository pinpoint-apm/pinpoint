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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author dawidmalina
 */
public class MariaDBJdbcUrlParser implements JdbcUrlParserV2 {

//    jdbc:(mysql|mariadb):[replication:|failover|loadbalance:|aurora:]//<hostDescription>[,<hostDescription>]/[database>]
//    jdbc:mariadb:loadbalance://10.22.33.44:3306,10.22.33.55:3306/MariaDB?characterEncoding=UTF-8
    static final String MARIA_URL_PREFIX = "jdbc:mariadb:";
    static final String MYSQL_URL_PREFIX = "jdbc:mysql:";

    private static final Set<Type> TYPES = EnumSet.allOf(Type.class);

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl");
            return UnKnownDatabaseInfo.INSTANCE;
        }

        Type type = getType(jdbcUrl);
        if (type == null) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, valid prefixes:{}, {})", jdbcUrl, MARIA_URL_PREFIX, MYSQL_URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        try {
            return parse0(jdbcUrl, type);
        } catch (Exception e) {
            logger.info("MaridDBJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            return UnKnownDatabaseInfo.createUnknownDataBase(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, jdbcUrl);
        }
    }

    private DatabaseInfo parse0(String url, Type type) {
        return parseNormal(url, type);
    }

    private DatabaseInfo parseNormal(String url, Type type) {
        // jdbc:mariadb://1.2.3.4:5678/test_db
        StringMaker maker = new StringMaker(url);
        maker.after(type.getUrlPrefix());
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();
        List<String> hostList = parseHost(host);
        // String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().after('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(MariaDBConstants.MARIADB, MariaDBConstants.MARIADB_EXECUTE_QUERY, url,
                normalizedUrl, hostList, databaseId);
    }

    private List<String> parseHost(String host) {
        final int multiHost = host.indexOf(",");
        if (multiHost == -1) {
            return Collections.singletonList(host);
        }
        // Decided not to cache regex. This is not invoked often so don't waste memory.
        String[] parsedHost = host.split(",");
        return Arrays.asList(parsedHost);
    }

    @Override
    public ServiceType getServiceType() {
        return MariaDBConstants.MARIADB;
    }

    private static Type getType(String jdbcUrl) {
        for (Type type : TYPES) {
            if (jdbcUrl.startsWith(type.getUrlPrefix())) {
                return type;
            }
        }
        return null;
    }

    private enum Type {
        MARIA(MARIA_URL_PREFIX),
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
            return loadbalanceUrlPrefix;
        }
    }
}
