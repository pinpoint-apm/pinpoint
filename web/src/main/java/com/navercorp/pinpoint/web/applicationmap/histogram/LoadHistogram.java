/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.LoadHistogramSerializer;

import java.util.Objects;

@JsonSerialize(using = LoadHistogramSerializer.class)
public class LoadHistogram {
    private Histogram histogram;

    public LoadHistogram(final Histogram histogram) {
        this.histogram = Objects.requireNonNull(histogram, "histogram");
    }

    public long getTotalErrorCount() {
        return histogram.getTotalErrorCount();
    }

    public long getFastCount() {
        return histogram.getFastCount();
    }

    public long getNormalCount() {
        return histogram.getNormalCount();
    }

    public long getSlowCount() {
        return histogram.getSlowCount();
    }

    public long getVerySlowCount() {
        return histogram.getVerySlowCount();
    }

    public long getAvgValue() {
        return histogram.getAvgElapsed();
    }

    public long getMaxValue() {
        return histogram.getMaxElapsed();
    }

    public long getSumValue() {
        return histogram.getSumElapsed();
    }

    public long getTotalCount() {
        return histogram.getTotalCount();
    }
}
