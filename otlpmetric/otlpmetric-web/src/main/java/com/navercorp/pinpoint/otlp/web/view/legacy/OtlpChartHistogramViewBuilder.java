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

package com.navercorp.pinpoint.otlp.web.view.legacy;

import com.navercorp.pinpoint.otlp.web.view.OtlpParsingException;

import java.util.List;

public class OtlpChartHistogramViewBuilder extends OtlpChartViewBuilder {
    private static final String FIELD_KEYWORD_COUNT = "count";
    private static final String FIELD_KEYWORD_SUM = "sum";
    private static final String FIELD_KEYWORD_MAX = "max";
    private static final String FIELD_KEYWORD_MIN = "min";
    private static final String METADATA_KEYWORD_NUMBUCKETS= "numBuckets";

    private long numBuckets;

    public OtlpChartHistogramViewBuilder() {
        super(CHART_TYPE_BAR);
    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        switch (fieldName) {
            case FIELD_KEYWORD_COUNT -> {
                this.description = description;
                return CHART_TYPE_SPLINE;
            }
            case FIELD_KEYWORD_SUM, FIELD_KEYWORD_MAX, FIELD_KEYWORD_MIN -> {
                return CHART_TYPE_SPLINE;
            }
            case METADATA_KEYWORD_NUMBUCKETS -> {
                return CHART_TYPE_NONE;
            }
            default -> {
                return CHART_TYPE_BAR;
            }
        }
    }

    @Override
    public void setMetadata(String name, List<Number> values, String description) {
        if (name.equals(METADATA_KEYWORD_NUMBUCKETS)) {
            int lastIndex = values.size() - 1;
            this.numBuckets = values.get(lastIndex).longValue();
            this.description = description;
        } else {
            throw new OtlpParsingException("Invalid metadata name: " + name);
        }
    }
}
