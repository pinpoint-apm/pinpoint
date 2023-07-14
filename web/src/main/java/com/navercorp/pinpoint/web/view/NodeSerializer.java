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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class NodeSerializer extends JsonSerializer<Node> {

    @Override
    public void serialize(Node node, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
//        jgen.writeStringField("id", node.getNodeName());serverInstanceList
        jgen.writeObjectField("key", node.getNodeName()); // necessary for go.js

        jgen.writeStringField("applicationName", node.getApplicationTextName()); // for go.js

        jgen.writeStringField("category", node.getServiceType().toString());  // necessary for go.js
        jgen.writeStringField("serviceType", node.getServiceType().toString());

        final ServiceType serviceType = node.getApplication().getServiceType();
//        if (serviceType.isUser()) {
//            jgen.writeStringField("fig", "Ellipse");
//        } else if(serviceType.isWas()) {
//            jgen.writeStringField("fig", "RoundedRectangle");
//        } else {
//            jgen.writeStringField("fig", "Rectangle");
//        }

        jgen.writeStringField("serviceTypeCode", Short.toString(serviceType.getCode()));
//        jgen.writeStringField("terminal", Boolean.toString(serviceType.isTerminal()));
        jgen.writeBooleanField("isWas", serviceType.isWas());  // for go.js
        jgen.writeBooleanField("isQueue", serviceType.isQueue());
        jgen.writeBooleanField("isAuthorized", node.isAuthorized());

        writeHistogram(node, jgen, provider);
        writeServerGroupList(jgen, node);

        jgen.writeEndObject();
    }


    private void writeServerGroupList(JsonGenerator jgen, Node node) throws IOException {
        ServerGroupList serverGroupList = node.getServerGroupList();
        if (node.getServiceType().isUnknown()) {
            serverGroupList = null;
        }

        final String agentIdNameMapKey = "agentIdNameMap";
        if (serverGroupList == null) {
            jgen.writeNumberField("instanceCount", 0);
            jgen.writeNumberField("instanceErrorCount", 0);
            writeEmptyArray(jgen, "agentIds");
            writeEmptyObject(jgen, agentIdNameMapKey);
            if (NodeType.DETAILED == node.getNodeType()) {
                writeEmptyObject(jgen, "serverList");
            }
        } else {
            jgen.writeNumberField("instanceCount", serverGroupList.getInstanceCount());
            long instanceErrorCount = 0;
            NodeHistogram nodeHistogram = node.getNodeHistogram();
            if (nodeHistogram != null) {
                Map<String, Histogram> agentHistogramMap = node.getNodeHistogram().getAgentHistogramMap();
                if (agentHistogramMap != null) {
                    instanceErrorCount = agentHistogramMap.values().stream()
                            .filter(agentHistogram -> agentHistogram.getTotalErrorCount() > 0)
                            .count();
                }
            }
            jgen.writeNumberField("instanceErrorCount", instanceErrorCount);
            jgen.writeArrayFieldStart("agentIds");
            for (String agentId : serverGroupList.getAgentIdList()) {
                jgen.writeString(agentId);
            }
            jgen.writeEndArray();

            jgen.writeObjectFieldStart(agentIdNameMapKey);
            for (Map.Entry<String, String> entry : serverGroupList.getAgentIdNameMap().entrySet()) {
                jgen.writeStringField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();

            if (NodeType.DETAILED == node.getNodeType()) {
                jgen.writeObjectField("serverList", serverGroupList);
            }
        }
    }

    private void writeHistogram(Node node, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        final ServiceType serviceType = node.getServiceType();
        final NodeHistogram nodeHistogram = node.getNodeHistogram();
        // FIXME isn't this all ServiceTypes that can be a node?
        if (serviceType.isWas() || serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isUser() || serviceType.isQueue() || serviceType.isAlias()) {
            Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
            if (applicationHistogram == null) {
                jgen.writeBooleanField("hasAlert", false);  // for go.js
            } else {
                jgen.writeNumberField("totalCount", applicationHistogram.getTotalCount()); // for go.js
                jgen.writeNumberField("errorCount", applicationHistogram.getTotalErrorCount());
                jgen.writeNumberField("slowCount", applicationHistogram.getSlowCount());

                if (applicationHistogram.getTotalCount() == 0) {
                    jgen.writeBooleanField("hasAlert", false);  // for go.js
                } else {
                    long error = applicationHistogram.getTotalErrorCount() / applicationHistogram.getTotalCount();
                    if (error * 100 > 10) {
                        jgen.writeBooleanField("hasAlert", true);  // for go.js
                    } else {
                        jgen.writeBooleanField("hasAlert", false);  // for go.js
                    }
                }
            }

            ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(applicationHistogram);
            jgen.writeObjectField(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);
            if (applicationHistogram == null) {
                writeEmptyObject(jgen, "histogram");
            } else {
                jgen.writeObjectField("histogram", applicationHistogram);
            }

            if (applicationHistogram == null) {
                writeEmptyObject(jgen, "apdexScore");
            } else {
                //jgen.writeObjectField("apdexScore", node.getApdexScore());
                JsonSerializer<Object> beanSerializer = provider.findValueSerializer(node.getApdexScore().getClass());
                JsonSerializer<Object> unwrapping = beanSerializer.unwrappingSerializer(NameTransformer.NOP);
                unwrapping.serialize(node.getApdexScore(), jgen, provider);
            }

            //agent histogram
            if (!node.isV3Format() && NodeType.DETAILED == node.getNodeType()) {
                Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
                if (agentHistogramMap == null) {
                    writeEmptyObject(jgen, "agentHistogram");
                    writeEmptyObject(jgen, ResponseTimeStatics.AGENT_RESPONSE_STATISTICS);
                } else {
                    jgen.writeObjectField("agentHistogram", agentHistogramMap);
                    jgen.writeObjectField(ResponseTimeStatics.AGENT_RESPONSE_STATISTICS, nodeHistogram.getAgentResponseStatisticsMap());
                }
            }
        } else {
            jgen.writeBooleanField("hasAlert", false);  // for go.js
        }

        //time histogram
        if (!node.isV3Format()) {
            // FIXME isn't this all ServiceTypes that can be a node?
            if (serviceType.isWas() || serviceType.isUser() || serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isQueue() || serviceType.isAlias()) {
                List<TimeViewModel> applicationTimeSeriesHistogram = nodeHistogram.getApplicationTimeHistogram(node.getTimeHistogramFormat());
                if (applicationTimeSeriesHistogram == null) {
                    writeEmptyArray(jgen, "timeSeriesHistogram");
                } else {
                    jgen.writeObjectField("timeSeriesHistogram", applicationTimeSeriesHistogram);
                }

                if (NodeType.DETAILED == node.getNodeType()) {
                    AgentResponseTimeViewModelList agentTimeSeriesHistogram = nodeHistogram.getAgentTimeHistogram(node.getTimeHistogramFormat());
                    jgen.writeObject(agentTimeSeriesHistogram);
                }
            }
        }
    }

    private void writeEmptyArray(JsonGenerator jgen, String fieldName) throws IOException {
        jgen.writeFieldName(fieldName);
        jgen.writeStartArray();
        jgen.writeEndArray();
    }

    private void writeEmptyObject(JsonGenerator jgen, String fieldName) throws IOException {
        jgen.writeFieldName(fieldName);
        jgen.writeStartObject();
        jgen.writeEndObject();
    }
}
