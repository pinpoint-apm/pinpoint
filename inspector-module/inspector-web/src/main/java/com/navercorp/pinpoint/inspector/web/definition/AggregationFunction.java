/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.definition;

/**
 * @author minwoo.jung
 */
public enum AggregationFunction {

    AVG(1, "avg"),
    SUM(2, "sum"),
    MAX(2, "max"),

    AVG_MIN_MAX(300, "avg_min_max"),

    AVG_MIN(301, "avg_min"),

    MIN_MAX(302, "min_max"),



    UNKNOWN(999, "unknown");

    private final int code;
    private final String value;

    AggregationFunction(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
