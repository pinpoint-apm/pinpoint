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

package com.navercorp.pinpoint.it.plugin.jdbc.oracle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class OracleJdbcUrlParser {

    private static final String URL_PREFIX = "jdbc:oracle:";

    public ParsedResult parse(String jdbcUrl) {
        StringMaker maker = new StringMaker(jdbcUrl);
        maker.after(URL_PREFIX).after(":");
        String description = maker.after('@').value().trim();
        return parseSimpleUrl(jdbcUrl, maker);
    }

    private ParsedResult parseSimpleUrl(String url, StringMaker maker) {
        String host = maker.before(':').value();
        String port = maker.next().after(':').before(':', '/').value();
        String databaseId = maker.next().afterLast(':', '/').value();

        List<String> hostList = new ArrayList<>(1);
        hostList.add(host + ":" + port);
        return new ParsedResult(databaseId, hostList.get(0));
    }

    class ParsedResult {
        public String databaseName;
        public String address;

        public ParsedResult(String databaseName, String address) {
            this.databaseName = databaseName;
            this.address = address;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getAddress() {
            return address;
        }
    }
}
