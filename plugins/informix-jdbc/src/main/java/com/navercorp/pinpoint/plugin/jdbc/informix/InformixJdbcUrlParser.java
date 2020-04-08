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

package com.navercorp.pinpoint.plugin.jdbc.informix;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Guillermo Adrian Molina
 */
public class InformixJdbcUrlParser implements JdbcUrlParserV2 {

    // jdbc:informix-sqli://serverName:portNumber/databaseName:[property=value[;property=value]]
    private static final String INFORMIX_URL_PREFIX = "jdbc:informix-sqli:";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl");
            return UnKnownDatabaseInfo.INSTANCE;
        }
        if (!jdbcUrl.startsWith(INFORMIX_URL_PREFIX)) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, prefix:{})", jdbcUrl, INFORMIX_URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo result = null;
        try {
            result = parse0(jdbcUrl);
        } catch (Exception e) {
            logger.info("InformixJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(InformixConstants.INFORMIX, InformixConstants.INFORMIX_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String url) {
        // jdbc:informix-sqli://123.45.67.89:1533/testDB:INFORMIXSERVER=myserver;user=rdtest;password=test

        StringMaker maker = new StringMaker(url);

        maker.after(INFORMIX_URL_PREFIX);

        String hostAndPort = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(hostAndPort);

        String databaseId = maker.next().after("/").before(':').value();
        String normalizedUrl = maker.clear().before(";").value();
        return new DefaultDatabaseInfo(InformixConstants.INFORMIX, InformixConstants.INFORMIX_EXECUTE_QUERY, url,
                normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return InformixConstants.INFORMIX;
    }

}
