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

package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Objects;

public class TimeHistogramBuilder {

    private final TimeHistogramFormat format;

    public TimeHistogramBuilder(TimeHistogramFormat format) {
        this.format = Objects.requireNonNull(format, "format");
    }

    public List<TimeHistogramViewModel> build(Application application, List<TimeHistogram> histogramList) {
        return switch (format) {
            case V1 -> new ResponseTimeViewModelBuilder(application, histogramList).build();
            case V2 -> throw new IllegalArgumentException("Unsupported V2 model");
            case V3 -> new TimeseriesHistogramViewModelBuilder(application, histogramList).build();
        };
    }

    public List<TimeHistogramViewModel> build(ApplicationTimeHistogram histogram) {
        Objects.requireNonNull(histogram, "histogram");

        return build(histogram.getApplication(), histogram.getHistogramList());
    }
}
