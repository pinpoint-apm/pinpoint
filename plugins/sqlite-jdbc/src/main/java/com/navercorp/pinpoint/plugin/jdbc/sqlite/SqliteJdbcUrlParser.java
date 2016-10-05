/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;

/**
 * SQLite JDBC URL example:
 * <ul>
 * <li>jdbc:sqlite:C:/work/mydatabase.db</li>
 * <li>jdbc:sqlite:/home/leo/work/mydatabase.db</li>
 * <li>jdbc:sqlite::memory:</li>
 * <li>jdbc:sqlite:</li>
 * <li>jdbc:sqlite::resource:org/yourdomain/sample.db</li>
 * <li>jdbc:sqlite::resource:http://www.xerial.org/svn/project/XerialJ/trunk/sqlite-jdbc/src/test/java/org/sqlite/sample.db</li>
 * <li>jdbc:sqlite::resource:jar:http://www.xerial.org/svn/project/XerialJ/trunk/sqlite-jdbc/src/test/resources/testdb.jar!/sample.db</li>
 * </ul>
 *
 * @author barney
 *
 * @see <a href="https://github.com/xerial/sqlite-jdbc/blob/master/Usage.md">SQLite JDBC Usage</a>
 */
public class SqliteJdbcUrlParser extends JdbcUrlParser {

    public static final String MEMORY = ":memory:";

    public static final String RESOURCE = ":resource:";

    private final Pattern RESOURCE_PATTERN = Pattern.compile(RESOURCE + "(jar:)?http(s?)://([a-zA-Z_0-9.-]+)/(.*)");

    public DatabaseInfo doParse(String url) {
        StringMaker maker = new StringMaker(url);
        maker.lower().after("jdbc:sqlite:");

        List<String> hosts = new ArrayList<String>(0);
        String databaseId = MEMORY;
        String details = defaultIfEmpty(maker.value(), MEMORY);
        if(!details.equals(MEMORY)) {
            databaseId = maker.afterLast('/').value();
            if(details.startsWith(RESOURCE) && (details.contains("http://") || details.contains("https://"))) {
                Matcher matcher = RESOURCE_PATTERN.matcher(details);
                if(matcher.matches()) {
                    hosts = new ArrayList<String>(1);
                    hosts.add(matcher.group(3));
                }
            }
        }
        return new DefaultDatabaseInfo(SqlitePluginConstants.SQLITE, SqlitePluginConstants.SQLITE_EXECUTE_QUERY, url, url, hosts, databaseId);
    }

    private String defaultIfEmpty(final String str, final String defaultStr) {
        return (str == null || str.length() == 0) ? defaultStr : str;
    }
}
