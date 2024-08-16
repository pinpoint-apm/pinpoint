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

package com.navercorp.pinpoint.otlp.common.definition.property;

import com.navercorp.pinpoint.common.server.util.EnumGetter;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo-jung
 */
public enum AggregationFunction {
    AVG(1, "avg"),
    SUM(2, "sum"),
    MIN(3, "min"),
    MAX(4, "max"),
    LATEST(5, "latest");

    private final int code;
    private final String functionName;

    AggregationFunction(int code, String functionName) {
        this.code = code;
        this.functionName = functionName;
    }

    public int getCode() {
        return code;
    }

    public String getAggregationFunctionName() {
        return functionName;
    }

    private static final EnumSet<AggregationFunction> ENUM_SET = EnumSet.allOf(AggregationFunction.class);
    private static final EnumGetter<AggregationFunction> GETTER = new EnumGetter<>(ENUM_SET);


    public static AggregationFunction fromAggregationFunctionName(String aggregationFunctionName) {
        return GETTER.fromValue(AggregationFunction::getAggregationFunctionName, aggregationFunctionName);
    }

    public static List<String> getAggregationFunctionNameList() {
        return ENUM_SET.stream()
                .map(AggregationFunction::getAggregationFunctionName)
                .collect(Collectors.toList());
    }
}
