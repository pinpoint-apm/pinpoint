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
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.view.id.AgentNameView;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public interface AgentTimeSeriesHistogramNodeView {

    default void writeAgentTimeSeriesHistogram(NodeView nodeView, JsonGenerator jgen) throws IOException {
    }

    static AgentTimeSeriesHistogramNodeView detailedView(TimeHistogramFormat format) {
        return new DetailedAgentHistogramNodeView(format);
    }

    static AgentTimeSeriesHistogramNodeView emptyView() {
        return new AgentTimeSeriesHistogramNodeView() {
        };
    }

    class DetailedAgentHistogramNodeView implements AgentTimeSeriesHistogramNodeView {
        private final TimeHistogramFormat format;

        public DetailedAgentHistogramNodeView(TimeHistogramFormat format) {
            this.format = Objects.requireNonNull(format, "format");
        }

        @Override
        public void writeAgentTimeSeriesHistogram(NodeView nodeView, JsonGenerator jgen) throws IOException {
            NodeHistogram nodeHistogram = nodeView.getNode().getNodeHistogram();
            AgentTimeHistogram agentTimeHistogram = nodeHistogram.getAgentTimeHistogram();

            JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentFields = agentTimeHistogram.createViewModel(format);
            jgen.writeFieldName("agentTimeSeriesHistogram");
            jgen.writeObject(agentFields);
        }
    }

}
