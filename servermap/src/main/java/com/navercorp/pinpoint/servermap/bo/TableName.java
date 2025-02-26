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
package com.navercorp.pinpoint.servermap.bo;

/**
 * @author intr3p1d
 */
public enum TableName {
    INBOUND("ApplicationMapInbound"),
    OUTBOUND("ApplicationMapOutbound"),
    SELF("ApplicationMapSelf");

    private final String name;

    TableName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TableName of(String tableName) {
        for (TableName value : values()) {
            if (value.getName().equals(tableName)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No enum constant " + TableName.class.getCanonicalName() + "." + tableName);
    }
}
