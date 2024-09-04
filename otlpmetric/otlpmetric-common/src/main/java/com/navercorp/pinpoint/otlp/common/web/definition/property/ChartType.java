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

package com.navercorp.pinpoint.otlp.common.web.definition.property;

import com.navercorp.pinpoint.common.server.util.EnumGetter;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo-jung
 */
public enum ChartType {

    LINE(1, "line"),
    AREA(2, "area"),
    BAR(3, "bar");

    private final int code;
    private final String chartName;

    ChartType(int code, String chartName) {
        this.code = code;
        this.chartName = chartName;
    }

    public int getCode() {
        return code;
    }

    public String getChartName() {
        return chartName;
    }

    private static final EnumSet<ChartType> ENUM_SET = EnumSet.allOf(ChartType.class);
    private static final EnumGetter<ChartType> GETTER = new EnumGetter<>(ENUM_SET);


    public static ChartType fromChartName(String chartName) {
        return GETTER.fromValueIgnoreCase(ChartType::getChartName, chartName);
    }

    public static List<String> getChartNameList() {
        return ENUM_SET.stream()
                .map(ChartType::getChartName)
                .collect(Collectors.toList());
    }
}
