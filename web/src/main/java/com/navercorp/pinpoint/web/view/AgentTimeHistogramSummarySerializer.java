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

/**
 * @author Taejin Koo
 */
public class AgentTimeHistogramSummarySerializer  extends JsonSerializer<AgentTimeHistogramSummary> {

    @Override
    public void serialize(AgentTimeHistogramSummary value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeFieldName(value.getAgentId());
        jgen.writeObject(value.getHistogram());
    }

}
