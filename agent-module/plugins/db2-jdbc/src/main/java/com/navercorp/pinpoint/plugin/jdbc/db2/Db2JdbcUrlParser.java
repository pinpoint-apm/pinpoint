/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.db2;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collections;
import java.util.List;

public class Db2JdbcUrlParser implements JdbcUrlParserV2 {

    private static final String URL_PREFIX = "jdbc:db2:";
    private static final String URL_REMOTE_INDICATOR = "//";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }
        if (!jdbcUrl.startsWith(URL_PREFIX)) {
            return UnKnownDatabaseInfo.INSTANCE;
        }

        try {
            return parse0(jdbcUrl);
        } catch (Exception e) {
            return UnKnownDatabaseInfo.createUnknownDataBase(Db2Constants.DB2, Db2Constants.DB2_EXECUTE_QUERY, jdbcUrl);
        }
    }

    private DatabaseInfo parse0(String url) {
        final String remainder = url.substring(URL_PREFIX.length());
        if (remainder.startsWith(URL_REMOTE_INDICATOR)) {
            // jdbc:db2://host:port/database[:prop=val;...]
            return parseRemoteUrl(url);
        }
        // jdbc:db2:database (Type 2, local)
        return parseLocalUrl(url, remainder);
    }

    private DatabaseInfo parseRemoteUrl(String url) {
        StringMaker maker = new StringMaker(url);
        maker.after(URL_PREFIX).after(URL_REMOTE_INDICATOR);
        String hosts = maker.before('/').value();
        List<String> hostList = StringUtils.tokenizeToStringList(hosts, ",");
        String databaseId = maker.next().afterLast('/').before(':').before(';').before('?').value();
        String normalizedUrl = URL_PREFIX + URL_REMOTE_INDICATOR + hosts + '/' + databaseId;
        return new DefaultDatabaseInfo(Db2Constants.DB2, Db2Constants.DB2_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }

    private DatabaseInfo parseLocalUrl(String url, String remainder) {
        String databaseId = new StringMaker(remainder).before(':').before(';').before('?').value();
        List<String> hostList = Collections.singletonList("localhost");
        String normalizedUrl = URL_PREFIX + databaseId;
        return new DefaultDatabaseInfo(Db2Constants.DB2, Db2Constants.DB2_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return Db2Constants.DB2;
    }
}
