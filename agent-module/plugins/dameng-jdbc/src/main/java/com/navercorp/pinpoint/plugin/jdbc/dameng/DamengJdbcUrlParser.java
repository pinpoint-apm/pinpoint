/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.dameng;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yjqg6666
 */
public class DamengJdbcUrlParser implements JdbcUrlParserV2 {

    static final String JDBC_PREFIX = "jdbc:dm://";

    private static final String DEFAULT_HOST = "localhost";

    private static final String DEFAULT_PORT = "5236";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl");
            return UnKnownDatabaseInfo.INSTANCE;
        }

        if (!jdbcUrl.startsWith(JDBC_PREFIX)) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, valid prefixes:{})", jdbcUrl, JDBC_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        try {
            return parseURL(jdbcUrl);
        } catch (Exception e) {
            logger.info("Dameng JdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            return UnKnownDatabaseInfo.createUnknownDataBase(DamengConstants.DAMENG, DamengConstants.DAMENG_EXECUTE_QUERY, jdbcUrl);
        }
    }

    private DatabaseInfo parseURL(String url) {
        StringMaker maker = new StringMaker(url);
        maker.after(JDBC_PREFIX);
        StringMaker maker2 = maker.duplicate();
        String hostPort = maker.before('?').value();
        String host = DEFAULT_HOST;
        String port = DEFAULT_PORT;
        if (StringUtils.hasText(hostPort)) {
            String[] hostPortInfo = hostPort.split(":", 2);
            host = hostPortInfo[0];
            if (hostPortInfo.length > 1) {
                port = hostPortInfo[1];
            }
        }

        String pHost = null;
        String pPort = null;
        String pGroup = null;
        boolean hasProp = url.indexOf("?") > 0;
        String propString = maker2.after('?').value();
        if (hasProp && StringUtils.hasText(propString)) {
            Map<String, String> queryMap = parseQuery(propString);
            pHost = queryMap.get("host");
            pPort = queryMap.get("port");
            boolean group = StringUtils.hasText(hostPort) && queryMap.containsKey(hostPort);
            if (group) {
                String groupValue = queryMap.get(hostPort);
                if (groupValue.startsWith("(") && groupValue.endsWith(")")) {
                    pGroup = groupValue.substring(1, groupValue.length() -1);
                }
            }
        }
        return databaseInfo(url, host, port, pHost, pPort, pGroup);
    }

    private DatabaseInfo databaseInfo(String url, String host, String port, String pHost, String pPort, String pGroup) {
        String hostPort;
        if (StringUtils.hasText(pGroup)) {
            hostPort = pGroup;
        } else {
            host = StringUtils.hasText(pHost) ? pHost : host;
            port = StringUtils.hasText(pPort) ? pPort : port;
            hostPort = host + ":" + port;
        }
        String normalizedUrl = JDBC_PREFIX + hostPort;
        List<String> hostList = parseHost(hostPort);
        return new DefaultDatabaseInfo(DamengConstants.DAMENG, DamengConstants.DAMENG_EXECUTE_QUERY, url,
                normalizedUrl, hostList, hostPort);
    }

    private Map<String, String> parseQuery(String propString) {
        String[] list = propString.replace("&amp;", "&").split("&");
        Map<String, String> map = new HashMap<>(list.length);
        for (String v : list) {
            if (!StringUtils.hasText(v)) {
                continue;
            }
            String[] kv = v.split("=", 2);
            if (kv.length > 1) {
                map.put(kv[0], kv[1]);
            } else {
                map.put(kv[0], StringUtils.EMPTY_STRING);
            }
        }
        return map;
    }

    private List<String> parseHost(String host) {
        final int multiHost = host.indexOf(',');
        if (multiHost == -1) {
            return Collections.singletonList(host);
        }
        String[] parsedHost = host.split(",");
        return Arrays.asList(parsedHost);
    }

    @Override
    public ServiceType getServiceType() {
        return DamengConstants.DAMENG;
    }

}
