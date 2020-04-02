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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.view.AgentTimeHistogramSummarySerializer;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = AgentTimeHistogramSummarySerializer.class)
public class AgentTimeHistogramSummary {

    private final String agentId;
    private final Histogram histogram;

    public static AgentTimeHistogramSummary createSummary(AgentHistogram agentHistogram) {
        Objects.requireNonNull(agentHistogram, "agentHistogram");
        return new AgentTimeHistogramSummary(agentHistogram.getAgentId().getName(), agentHistogram.getHistogram());
    }

    public AgentTimeHistogramSummary(String agentId, Histogram histogram) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.histogram = Objects.requireNonNull(histogram, "histogram");
    }

    public String getAgentId() {
        return agentId;
    }

    public Histogram getHistogram() {
        return histogram;
    }

}
