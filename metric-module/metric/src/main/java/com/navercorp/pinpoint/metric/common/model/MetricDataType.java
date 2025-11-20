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

package com.navercorp.pinpoint.metric.common.model;

import java.util.Objects;

public enum MetricDataType {
    LONG(1, "long"),
    DOUBLE(2, "double"),
    UNKNOWN(-1, "unknown");

    private final int code;
    private final String type;

    MetricDataType(int code, String type) {
        this.code = code;
        this.type = Objects.requireNonNull(type, "type");
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public static MetricDataType getByCode(int code) {
        return switch (code) {
            case 1 -> LONG;
            case 2 -> DOUBLE;
            case -1 -> UNKNOWN;
            default -> throw new IllegalArgumentException("Unknown code : " + code);
        };
    }

    public static MetricDataType getByType(String name) {
        return switch (name) {
            case "long" -> LONG;
            case "double" -> DOUBLE;
            case "unknown" -> UNKNOWN;
            default -> throw new IllegalArgumentException("Unknown name : " + name);
        };
    }
}
