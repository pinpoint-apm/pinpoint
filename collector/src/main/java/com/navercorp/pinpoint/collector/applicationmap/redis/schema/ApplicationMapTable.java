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
package com.navercorp.pinpoint.collector.applicationmap.redis.schema;

/**
 * @author intr3p1d
 */
public enum ApplicationMapTable {
    Inbound("ApplicationMapInbound"),
    Outbound("ApplicationMapOutbound"),
    Self("ApplicationMapSelf");

    private final String table;

    ApplicationMapTable(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }
}
