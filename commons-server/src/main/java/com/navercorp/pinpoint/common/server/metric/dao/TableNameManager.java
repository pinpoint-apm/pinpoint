/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.metric.dao;

import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;

/**
 * @author minwoo-jung
 */
public class TableNameManager {

    private final String tablePrefix;
    private final String numberFormat;
    private final int count;

    public TableNameManager(String tablePrefix, int paddingLength, int count) {
        this.tablePrefix = tablePrefix;
        this.numberFormat = "%0" + paddingLength + "d";
        this.count = count;
    }

    public String getTableName(String applicationName) {
        int hashValue = getHashValue(applicationName);
        String postfix = String.format(numberFormat, hashValue);
        return tablePrefix.concat(postfix);
    }

    protected int getHashValue(String applicationName) {
        int hash = Utils.murmur2(applicationName.getBytes(StandardCharsets.UTF_8));
        return Utils.toPositive(hash) % count;
    }

}
