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
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface AgentLinkView {
    default void writeAgentLink(LinkView linkView, JsonGenerator jgen) throws IOException {
    }

    static AgentLinkView detailedView(TimeHistogramFormat format) {
        return new AgentLinkView.DetailedAgentLinkView(format);
    }


    static AgentLinkView emptyView() {
        return new AgentLinkView() {
        };
    }

    class DetailedAgentLinkView implements AgentLinkView {

        private final TimeHistogramFormat format;

        public DetailedAgentLinkView(TimeHistogramFormat format) {
            this.format = Objects.requireNonNull(format, "format");
        }

        @Override
        public void writeAgentLink(LinkView linkView, JsonGenerator jgen) throws IOException {
            Link link = linkView.getLink();
            final List<AgentHistogram> sourceList = link.getSourceList().getAgentHistogramList();
            writeAgentHistogram("sourceHistogram", sourceList, jgen);
            writeAgentResponseStatistics("sourceResponseStatistics", sourceList, jgen);

            final List<AgentHistogram> targetList = link.getTargetList().getAgentHistogramList();
            writeAgentHistogram("targetHistogram", targetList, jgen);
            writeAgentResponseStatistics("targetResponseStatistics", targetList, jgen);

            writeSourceAgentTimeSeriesHistogram(linkView, jgen);
        }

        private void writeAgentHistogram(String fieldName, Collection<AgentHistogram> agentHistogramList, JsonGenerator jgen) throws IOException {
            jgen.writeFieldName(fieldName);
            jgen.writeStartObject();
            for (AgentHistogram agentHistogram : agentHistogramList) {
                jgen.writeFieldName(agentHistogram.getId());
                jgen.writeObject(agentHistogram.getHistogram());
            }
            jgen.writeEndObject();
        }

        private void writeAgentResponseStatistics(String fieldName, Collection<AgentHistogram> agentHistogramList, JsonGenerator jgen) throws IOException {
            jgen.writeFieldName(fieldName);
            jgen.writeStartObject();
            for (AgentHistogram agentHistogram : agentHistogramList) {
                jgen.writeFieldName(agentHistogram.getId());
                jgen.writeObject(ResponseTimeStatics.fromHistogram(agentHistogram.getHistogram()));
            }
            jgen.writeEndObject();
        }

        private void writeSourceAgentTimeSeriesHistogram(LinkView linkView, JsonGenerator jgen) throws IOException {
            Link link = linkView.getLink();
            AgentTimeHistogram agentTimeHistogram = link.getSourceAgentTimeSeriesHistogram();
            JsonFields<AgentNameView, List<TimeHistogramViewModel>> sourceAgentTimeSeriesHistogram = agentTimeHistogram.createViewModel(format);
            jgen.writeFieldName("sourceTimeSeriesHistogram");
            jgen.writeObject(sourceAgentTimeSeriesHistogram);
        }
    }
}
