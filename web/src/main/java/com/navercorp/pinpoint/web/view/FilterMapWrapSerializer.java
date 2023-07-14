/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterData;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterDataV3;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterScanResult;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWrap;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class FilterMapWrapSerializer extends JsonSerializer<FilterMapWrap> {
    @Override
    public void serialize(FilterMapWrap wrap, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeObjectField("applicationMapData", wrap.getApplicationMap());
        jgen.writeNumberField("lastFetchedTimestamp", wrap.getLastFetchedTimestamp());

        if (wrap.getApplicationMap() instanceof ApplicationMapWithScatterScanResult) {
            final List<ApplicationScatterScanResult> applicationScatterScanResult = ((ApplicationMapWithScatterScanResult) wrap.getApplicationMap()).getApplicationScatterScanResultList();

            jgen.writeFieldName("applicationScatterScanResult");
            jgen.writeStartObject();
            for (ApplicationScatterScanResult scatterScanResult : applicationScatterScanResult) {
                NodeName nodeName = NodeName.of(scatterScanResult.getApplication());
                jgen.writeObjectField(nodeName.getName(), scatterScanResult.getScatterScanResult());
            }
            jgen.writeEndObject();
        }

        if (wrap.getApplicationMap() instanceof ApplicationMapWithScatterData) {
            Map<Application, ScatterData> applicationScatterDataMap = ((ApplicationMapWithScatterData) wrap.getApplicationMap()).getApplicationScatterDataMap();

            writeScatterData(jgen, applicationScatterDataMap);
        }

        if (wrap.getApplicationMap() instanceof ApplicationMapWithScatterDataV3) {
            ApplicationMapWithScatterDataV3 applicationMap = (ApplicationMapWithScatterDataV3) wrap.getApplicationMap();
            Map<Application, ScatterData> applicationScatterDataMap = applicationMap.getApplicationScatterDataMap();

            writeScatterData(jgen, applicationScatterDataMap);
            jgen.writeObjectField("nodeHistogramDataArray", applicationMap.getNodeHistogramData());
            jgen.writeObjectField("linkHistogramDataArray", applicationMap.getLinkHistogramData());
            jgen.writeObjectField("agentHistogramDataArray", applicationMap.getAgentHistogramData());
        }

        jgen.writeEndObject();
    }

    private void writeScatterData(JsonGenerator jgen, Map<Application, ScatterData> applicationScatterDataMap) throws IOException {
        jgen.writeFieldName("applicationScatterData");
        jgen.writeStartObject();

        for (Map.Entry<Application, ScatterData> entry : applicationScatterDataMap.entrySet()) {
            NodeName nodeName = NodeName.of(entry.getKey());
            jgen.writeFieldName(nodeName.getName());

            ScatterData scatterData = entry.getValue();

            jgen.writeStartObject();
            jgen.writeObjectField("from", scatterData.getFrom());
            jgen.writeObjectField("to", scatterData.getTo());
            jgen.writeObjectField("resultFrom", scatterData.getOldestAcceptedTime());
            jgen.writeObjectField("resultTo", scatterData.getLatestAcceptedTime());
            jgen.writeObjectField("scatter", scatterData);
            jgen.writeEndObject();
        }

        jgen.writeEndObject();
    }
}
