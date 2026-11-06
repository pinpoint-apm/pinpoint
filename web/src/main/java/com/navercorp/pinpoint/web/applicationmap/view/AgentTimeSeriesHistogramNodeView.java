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

import tools.jackson.core.JsonGenerator;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.view.id.AgentNameView;

import java.util.List;
import java.util.Objects;

public interface AgentTimeSeriesHistogramNodeView {

    default void writeAgentTimeSeriesHistogram(NodeView nodeView, JsonGenerator jgen) {
    }

    static AgentTimeSeriesHistogramNodeView detailedView(TimeHistogramView timeHistogramView) {
        return new DetailedAgentHistogramNodeView(timeHistogramView);
    }

    static AgentTimeSeriesHistogramNodeView emptyView() {
        return new AgentTimeSeriesHistogramNodeView() {
        };
    }

    class DetailedAgentHistogramNodeView implements AgentTimeSeriesHistogramNodeView {
        private final TimeHistogramView timeHistogramView;

        public DetailedAgentHistogramNodeView(TimeHistogramView timeHistogramView) {
            this.timeHistogramView = Objects.requireNonNull(timeHistogramView, "timeHistogramView");
        }

        @Override
        public void writeAgentTimeSeriesHistogram(NodeView nodeView, JsonGenerator jgen) {
            NodeHistogram nodeHistogram = nodeView.getNode().getNodeHistogram();
            AgentTimeHistogram agentTimeHistogram = nodeHistogram.getAgentTimeHistogram();

            JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentFields = timeHistogramView.build(agentTimeHistogram);
            jgen.writeName("agentTimeSeriesHistogram");
            jgen.writePOJO(agentFields);
        }
    }

}
