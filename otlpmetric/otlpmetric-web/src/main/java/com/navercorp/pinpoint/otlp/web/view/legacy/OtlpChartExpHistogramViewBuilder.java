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

import java.util.List;

public class OtlpChartExpHistogramViewBuilder extends OtlpChartViewBuilder {

    public OtlpChartExpHistogramViewBuilder() {
        super(CHART_TYPE_BAR);
    }

    @Override
    protected void checkValidity(int timestampSize) {

    }

    @Override
    protected String checkChartType(String fieldName, String description) {
        return null;
    }

    @Override
    protected void setMetadata(String name, List<Number> values, String description) {
        throw new UnsupportedOperationException("ExpHistogramViewBuilder does not support metadata.");

    }
}
