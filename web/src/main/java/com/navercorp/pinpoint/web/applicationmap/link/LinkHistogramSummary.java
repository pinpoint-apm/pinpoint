/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.link;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.view.LinkHistogramSummarySerializer;
import com.navercorp.pinpoint.web.view.ResponseTimeViewModel;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = LinkHistogramSummarySerializer.class)
public class LinkHistogramSummary {

    private final String linkName;
    private final Histogram histogram;
    private final List<ResponseTimeViewModel> timeSeriesHistogram;

    public LinkHistogramSummary(Link link) {
        if (link == null) {
            throw new NullPointerException("link must not be null");
        }
        linkName = link.getLinkName();
        histogram = link.getHistogram();
        timeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram();
    }

    public String getLinkName() {
        return linkName;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public List<ResponseTimeViewModel> getTimeSeriesHistogram() {
        return timeSeriesHistogram;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LinkHistogramSummary{");
        sb.append("linkName='").append(linkName).append('\'');
        sb.append(", histogram=").append(histogram);
        sb.append(", timeSeriesHistogram=").append(timeSeriesHistogram);
        sb.append('}');
        return sb.toString();
    }
}
