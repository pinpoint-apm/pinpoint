/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramSummary;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ApplicationTimeHistogramViewModelSerializer extends JsonSerializer<ApplicationTimeHistogramViewModel> {

    @Override
    public void serialize(ApplicationTimeHistogramViewModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        writeSummary(value.getSummaryList(), jgen);
        writeTimeSeries(value.getTimeSeriesViewModel(), jgen);

        jgen.writeEndObject();
    }

    private void writeTimeSeries(List<AgentResponseTimeViewModel> timeSeriesViewModel, JsonGenerator jgen) throws IOException {
        jgen.writeObjectFieldStart("timeSeries");
        for (AgentResponseTimeViewModel agentResponseTimeViewModel : timeSeriesViewModel) {
            jgen.writeObjectField(agentResponseTimeViewModel.getAgentName(), agentResponseTimeViewModel.getResponseTimeViewModel());
        }
        jgen.writeEndObject();
    }

    private void writeSummary(List<AgentTimeHistogramSummary> summaryList, JsonGenerator jgen) throws IOException {
        jgen.writeObjectFieldStart("summary");
        for (AgentTimeHistogramSummary summary : summaryList) {
            jgen.writeObjectField(summary.getAgentId(), summary.getHistogram());
        }
        jgen.writeEndObject();
    }

}
