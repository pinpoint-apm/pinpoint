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

package com.navercorp.pinpoint.plugin.jdbc.clickhouse;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class ClickHouseJdbcUrlParser implements JdbcUrlParserV2 {

    // >=0.3.2
    // jdbc:(ch|clickhouse)[:protocol]://endpoint[,endpoint][/database][?parameters][#tags]
    static final String URL_PREFIX = "jdbc:clickhouse:";
    static final String URL_SHORT_PREFIX = "jdbc:ch:";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl must not be null");
            return UnKnownDatabaseInfo.INSTANCE;
        }
        if (!jdbcUrl.startsWith(URL_PREFIX) && !jdbcUrl.startsWith(URL_SHORT_PREFIX)) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, prefix:{})", jdbcUrl, URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo result = null;
        try {
            logger.info("jdbcUrl={}",jdbcUrl);
            result = parse0(jdbcUrl);
        } catch (Exception e) {
            logger.info("ClickHouseJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(ClickHouseConstants.CLICK_HOUSE, ClickHouseConstants.CLICK_HOUSE_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String jdbcUrl) {
        // jdbc:(ch|clickhouse)[:protocol]://endpoint[,endpoint][/database][?parameters][#tags]
        StringMaker maker = new StringMaker(jdbcUrl);
        maker.after(URL_PREFIX);
        maker.after(URL_SHORT_PREFIX);

        // [:protocol]://endpoint[,endpoint][/database][?parameters][#tags]
        // endpoint: [protocol://]host[:port][/database][?parameters][#tags]
        // protocol: (grpc|grpcs|http|https|tcp|tcps)
        // 1.2.3.4:5678 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").beforeLast('/').value();
        List<String> hostList = parseHost(host);
        String databaseId = maker.next().after('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(ClickHouseConstants.CLICK_HOUSE, ClickHouseConstants.CLICK_HOUSE_EXECUTE_QUERY, jdbcUrl, normalizedUrl, hostList, databaseId);
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
        return ClickHouseConstants.CLICK_HOUSE;
    }

}
