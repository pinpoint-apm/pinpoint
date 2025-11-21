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
import com.navercorp.pinpoint.common.server.util.json.JacksonWriterUtils;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public interface ApplicationTimeSeriesHistogramNodeView {

    default void writeTimeSeriesHistogram(NodeView nodeView, JsonGenerator jgen) throws IOException {
    }

    static ApplicationTimeSeriesHistogramNodeView detailedView(TimeHistogramView timeHistogramView) {
        return new DetailedAgentHistogramNodeView(timeHistogramView);
    }

    static ApplicationTimeSeriesHistogramNodeView emptyView() {
        return new ApplicationTimeSeriesHistogramNodeView() {
        };
    }

    class DetailedAgentHistogramNodeView implements ApplicationTimeSeriesHistogramNodeView {

        private final TimeHistogramView timeHistogramView;

        public DetailedAgentHistogramNodeView(TimeHistogramView timeHistogramView) {
            this.timeHistogramView = Objects.requireNonNull(timeHistogramView, "timeHistogramView");
        }

        @Override
        public void writeTimeSeriesHistogram(NodeView nodeView, JsonGenerator jgen) throws IOException {
            NodeHistogram nodeHistogram = nodeView.getNode().getNodeHistogram();
            ApplicationTimeHistogram applicationTimeHistogram = nodeHistogram.getApplicationTimeHistogram();
            List<TimeHistogramViewModel> applicationTimeSeriesHistogram = timeHistogramView.build(applicationTimeHistogram);
            if (applicationTimeSeriesHistogram == null) {
                JacksonWriterUtils.writeEmptyArray(jgen, "timeSeriesHistogram");
            } else {
                jgen.writeObjectField("timeSeriesHistogram", applicationTimeSeriesHistogram);
            }
        }
    }
}
