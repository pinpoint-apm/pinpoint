/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.oracle.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.navercorp.pinpoint.plugin.jdbc.oracle.parser.ParserUtils.compare;

public class DescriptionList implements DatabaseSpec {
    public static final String DESCRIPTION_LIST = "description_list";

    private final List<Description> descriptionList = new ArrayList<>();

    public DescriptionList(KeyValue<?> keyValue) {
        if (keyValue instanceof KeyValue.TerminalKeyValue) {
            if (!compare(DESCRIPTION_LIST, keyValue)) {
                throw new OracleConnectionStringException(DESCRIPTION_LIST + " node not found");
            }
        }
        if (keyValue instanceof KeyValue.KeyValueList) {
            KeyValue.KeyValueList kvList = (KeyValue.KeyValueList) keyValue;
            for (KeyValue<?> desc : kvList.getValue()) {
                if (compare(Description.DESCRIPTION, desc)) {
                    Description description = new Description(desc);
                    this.descriptionList.add(description);
                }
            }
        }
    }

    public List<Description> getDescriptionList() {
        return descriptionList;
    }

    @Override
    public String getDatabaseId() {
        Set<String> databaseIds = new HashSet<>();
        for (Description description : descriptionList) {
            databaseIds.add(description.getDatabaseId());
        }
        if (databaseIds.size() == 1) {
            return databaseIds.iterator().next();
        }
        return databaseIds.toString();
    }

    @Override
    public List<String> getJdbcHost() {
        List<String> jdbcHost = new ArrayList<>();
        for (Description description : descriptionList) {
            jdbcHost.addAll(description.getJdbcHost());
        }
        return jdbcHost;
    }

}
