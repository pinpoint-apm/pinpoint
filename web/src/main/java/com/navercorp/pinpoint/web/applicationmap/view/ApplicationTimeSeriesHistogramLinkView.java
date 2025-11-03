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

import com.fasterxml.jackson.core.JsonGenerator;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public interface ApplicationTimeSeriesHistogramLinkView {

    default void writeTimeSeriesHistogram(LinkView linkView, JsonGenerator jgen) throws IOException {
    }

    static ApplicationTimeSeriesHistogramLinkView detailedView(TimeHistogramFormat format) {
        return new DetailedAgentHistogramNodeView(format);
    }

    static ApplicationTimeSeriesHistogramLinkView emptyView() {
        return new ApplicationTimeSeriesHistogramLinkView() {
        };
    }

    class DetailedAgentHistogramNodeView implements ApplicationTimeSeriesHistogramLinkView {

        private final TimeHistogramFormat format;

        public DetailedAgentHistogramNodeView(TimeHistogramFormat format) {
            this.format = Objects.requireNonNull(format, "format");
        }

        @Override
        public void writeTimeSeriesHistogram(LinkView linkView, JsonGenerator jgen) throws IOException {
            Link link = linkView.getLink();
            ApplicationTimeHistogram linkApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram();
            TimeHistogramBuilder builder = new TimeHistogramBuilder(format);
            List<TimeHistogramViewModel> sourceApplicationHistogram = builder.build(linkApplicationTimeSeriesHistogram);
            jgen.writeFieldName("timeSeriesHistogram");
            jgen.writeObject(sourceApplicationHistogram);
        }
    }
}
