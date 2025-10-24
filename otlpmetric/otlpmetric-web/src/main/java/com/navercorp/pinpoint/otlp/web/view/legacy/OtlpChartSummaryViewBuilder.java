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

package com.navercorp.pinpoint.otlp.web.view.legacy;

import com.navercorp.pinpoint.otlp.web.view.OtlpParsingException;

import java.util.List;

public class OtlpChartSummaryViewBuilder extends OtlpChartViewBuilder {


    private static final String FIELD_KEYWORD_COUNT = "count";
    private static final String FIELD_KEYWORD_SUM = "sum";
    private static final String METADATA_KEYWORD_NUMQUANTILES = "numQuantiles";

    private List<Number> numQuantiles;
    //private Map<String, String> quantileMap; // ?? it can change in time

    public OtlpChartSummaryViewBuilder() {
        super(CHART_TYPE_AREA);
    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        if (FIELD_KEYWORD_COUNT.equals(fieldName) || FIELD_KEYWORD_SUM.equals(fieldName)) {
            return CHART_TYPE_SPLINE;
        } else if (METADATA_KEYWORD_NUMQUANTILES.equals(fieldName)) {
            return CHART_TYPE_NONE;
        } else {
            return CHART_TYPE_AREA;
        }
    }

    @Override
    protected void setMetadata(String name, List<Number> values, String description) {
        if (METADATA_KEYWORD_NUMQUANTILES.equals(name)) {
            this.numQuantiles = values;
            this.description = description;
        } else {
            throw new OtlpParsingException("Invalid metadata name: " + name);
        }

    }
}
