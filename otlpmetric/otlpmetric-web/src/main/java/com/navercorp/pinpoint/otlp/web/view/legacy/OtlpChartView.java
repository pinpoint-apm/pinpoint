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

import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.web.view.OtlpParsingException;

import java.util.ArrayList;
import java.util.List;

public class OtlpChartView {
    private List<Long> timestamp;
    private String description;
    private boolean hasSummary = false;
    private List<OtlpChartFieldView> fields;
    //private OtlpChartFieldViewBuilder chartFieldViewBuilder;

    public OtlpChartView(List<Long> timestamp, String description, boolean hasSummary, List<OtlpChartFieldView> fields) {
        this.timestamp = timestamp;
        this.description = description;
        this.hasSummary = hasSummary;
        this.fields = fields;
    }

    public OtlpChartView(int chartType) {
        MetricType metricType = MetricType.forNumber(chartType);
        if (metricType == null) {
            throw new OtlpParsingException("Cannot get valid metric type for requested chart.");
        }
        fields = new ArrayList<>();
    }

    public List<Long> getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public List<OtlpChartFieldView> getFields() {
        return fields;
    }

    /*
    private void checkValidity(MetricType chartType) {
        // make chart with above data
        switch (chartType) {
            case SUM:
            case GAUGE:
                this.chartFieldViewBuilder = new OtlpChartSumGaugeFieldViewBuilder();
                this.hasSummary = true;
                break;
            case HISTOGRAM:
                this.chartFieldViewBuilder = new OtlpChartHistogramFieldViewBuilder();
                break;
            case EXP_HISTOGRAM:
                //this.chartFieldViewBuilder = new OtlpChartHistogramFieldViewBuilder();
                break;
            case SUMMARY:
                //this.chartType = "area-spline";
                break;
            default:
                throw new OtlpParsingException("Should not reach here.");
        }
    }*/
}
