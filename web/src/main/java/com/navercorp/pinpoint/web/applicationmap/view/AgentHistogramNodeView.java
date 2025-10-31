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
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.io.IOException;
import java.util.Map;

public interface AgentHistogramNodeView {

    default void writeAgentHistogram(NodeView nodeView, JsonGenerator jgen) throws IOException {
    }

    static AgentHistogramNodeView detailedView() {
        return new DetailedAgentHistogramNodeView();
    }

    static AgentHistogramNodeView emptyView() {
        return new AgentHistogramNodeView() {
        };
    }

    class DetailedAgentHistogramNodeView implements AgentHistogramNodeView {

        public void writeAgentHistogram(NodeView nodeView, JsonGenerator jgen) throws IOException {
            NodeHistogram nodeHistogram = nodeView.getNode().getNodeHistogram();
            Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
            if (agentHistogramMap == null) {
                JacksonWriterUtils.writeEmptyObject(jgen, "agentHistogram");
                JacksonWriterUtils.writeEmptyObject(jgen, ResponseTimeStatics.AGENT_RESPONSE_STATISTICS);
            } else {
                jgen.writeObjectField("agentHistogram", agentHistogramMap);
                jgen.writeObjectField(ResponseTimeStatics.AGENT_RESPONSE_STATISTICS, nodeHistogram.getAgentResponseStatisticsMap());
            }
        }
    }
}
