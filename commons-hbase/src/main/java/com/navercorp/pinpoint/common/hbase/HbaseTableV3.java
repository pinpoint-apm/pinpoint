/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

public enum HbaseTableV3 implements HbaseTable {

    MAP_APP_SELF("MapAppSelf"),
    MAP_APP_OUT("MapAppOut"),
    MAP_APP_IN("MapAppIn"),

    MAP_APP_HOST("MapAppHost");

    private final String name;

    HbaseTableV3(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
