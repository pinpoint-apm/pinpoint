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
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.view.LinkHistogramSummarySerializer;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramType;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = LinkHistogramSummarySerializer.class)
public class LinkHistogramSummary {

    private final Link link;

    public LinkHistogramSummary(Link link) {
        Objects.requireNonNull(link, "link");
        this.link = link;
    }

    public void setTimeHistogramFormat(TimeHistogramFormat timeHistogramFormat) {
        link.setTimeHistogramFormat(timeHistogramFormat);
    }

    public LinkName getLinkName() {
        return link.getLinkName();
    }

    public Histogram getHistogram() {
        return link.getHistogram();
    }

    public List<TimeViewModel> getTimeSeriesHistogram() {
        return link.getLinkApplicationTimeSeriesHistogram();
    }

    public TimeSeriesView getTimeHistogram(TimeHistogramType timeHistogramType) {
        return link.getLinkApplicationTimeSeriesHistogram(timeHistogramType);
    }

    @Override
    public String toString() {
        return "LinkHistogramSummary{" +
                "link=" + link +
                '}';
    }
}
