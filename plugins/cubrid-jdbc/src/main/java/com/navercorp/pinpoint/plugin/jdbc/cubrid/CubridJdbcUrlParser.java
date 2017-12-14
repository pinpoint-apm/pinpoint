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

package com.navercorp.pinpoint.plugin.jdbc.cubrid;

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
 * @author emeroad
 */
public class CubridJdbcUrlParser implements JdbcUrlParserV2 {

    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 30000;
    public static final String DEFAULT_USER = "public";
    public static final String DEFAULT_PASSWORD = "";

    private static final String URL_PREFIX_PATTERN = "jdbc:cubrid(-oracle|-mysql)?:";
    private static final Pattern PREFIX_PATTERN = Pattern.compile(URL_PREFIX_PATTERN, Pattern.CASE_INSENSITIVE);

    private static final String URL_PATTERN = URL_PREFIX_PATTERN + "([a-zA-Z_0-9\\.-]*):([0-9]*):([^:]+):([^:]*):([^:]*):(\\?[a-zA-Z_0-9]+=[^&=?]+(&[a-zA-Z_0-9]+=[^&=?]+)*)?";
    private static final Pattern PATTERN = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl must not be null");
            return UnKnownDatabaseInfo.INSTANCE;
        }
        final Matcher matcher = PREFIX_PATTERN.matcher(jdbcUrl);
        if (!matcher.find()) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, prefix-pattern:{})", jdbcUrl, URL_PREFIX_PATTERN);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo result = null;
        try {
            result = parse0(jdbcUrl);
        } catch (Exception e) {
            logger.info("CubridJdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(CubridConstants.CUBRID, CubridConstants.CUBRID_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String jdbcUrl) {
        final Matcher matcher = PATTERN.matcher(jdbcUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException();
        }

        String host = matcher.group(2);
        String portString = matcher.group(3);
        String db = matcher.group(4);
        String user = matcher.group(5);
//        String pass = matcher.group(6);
//        String prop = matcher.group(7);

//        int port = DEFAULT_PORT;

//        String resolvedUrl;

        if (StringUtils.isEmpty(host)) {
            host = DEFAULT_HOSTNAME;
        }

//        if (portString == null || portString.length() == 0) {
//            port = DEFAULT_PORT;
//        } else {
//            try {
//                port = Integer.parseInt(portString);
//            } catch (NumberFormatException e) {
//                logger.info("cubrid portString parsing fail. portString:{}, url:{}", portString, jdbcUrl);
//            }
//        }

        if (user == null) {
            user = DEFAULT_USER;
        }

//        if (pass == null) {
//            pass = DEFAULT_PASSWORD;
//        }

//        resolvedUrl = "jdbc:cubrid:" + host + ":" + port + ":" + db + ":" + user + ":********:";

        StringMaker maker = new StringMaker(jdbcUrl);
        String normalizedUrl = maker.clear().before('?').value();

        List<String> hostList = new ArrayList<String>(1);
        final String hostAndPort = host + ":" + portString;
        hostList.add(hostAndPort);

        // skip alt host

        return new DefaultDatabaseInfo(CubridConstants.CUBRID, CubridConstants.CUBRID_EXECUTE_QUERY, jdbcUrl, normalizedUrl, hostList, db);
    }

    /*
    private DatabaseInfo parseCubrid(String url) {
        // jdbc:cubrid:10.20.30.40:12345:pinpoint:::
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:cubrid:");
        // 10.11.12.13:3306 In case of replication driver could have multiple values
        // We have to consider mm db too.
        String host = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
        // String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();

        return new DatabaseInfo(ServiceType.CUBRID, ServiceType.CUBRID_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }
    */


    @Override
    public ServiceType getServiceType() {
        return CubridConstants.CUBRID;
    }

}
