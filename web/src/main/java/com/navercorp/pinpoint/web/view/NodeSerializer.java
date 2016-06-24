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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.vo.Application;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author emeroad
 * @author minwoo.jung
 */
public class NodeSerializer extends JsonSerializer<Node>  {
    
    @Autowired(required=false)
    private ServerMapDataFilter serverMapDataFilter;
    
    @Override
    public void serialize(Node node, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        final boolean isAuthorized = check(node.getApplication());
        
        jgen.writeStartObject();
//        jgen.writeStringField("id", node.getNodeName());
        jgen.writeStringField("key", node.getNodeName(isAuthorized)); // necessary for go.js

        jgen.writeStringField("applicationName", node.getApplicationTextName()); // for go.js
        jgen.writeBooleanField("isAuthorized", isAuthorized);
        writeServiceTypeInfo(jgen, node, isAuthorized);
//        if (serviceType.isUser()) {
//            jgen.writeStringField("fig", "Ellipse");
//        } else if(serviceType.isWas()) {
//            jgen.writeStringField("fig", "RoundedRectangle");
//        } else {
//            jgen.writeStringField("fig", "Rectangle");
//        }

//        jgen.writeStringField("terminal", Boolean.toString(serviceType.isTerminal()));
        writeHistogram(jgen, node, isAuthorized);
        writeServerList(jgen, node, isAuthorized);

        jgen.writeEndObject();
    }
    
    private void writeServerList(JsonGenerator jgen, Node node, boolean isAuthorized) throws IOException {
        if (isAuthorized == false) {
            return;
        }
        
        if (node.getServiceType().isUnknown()) {
            writeEmptyObject(jgen, "serverList");
            jgen.writeNumberField("instanceCount", 0);
        } else {
            final ServerInstanceList serverInstanceList = node.getServerInstanceList();
            if (serverInstanceList != null) {
                jgen.writeObjectField("serverList", serverInstanceList);
                jgen.writeNumberField("instanceCount", serverInstanceList.getInstanceCount());
            } else {
                writeEmptyObject(jgen, "serverList");
                jgen.writeNumberField("instanceCount", 0);
            }
        }
    }
    
    private void writeServiceTypeInfo(JsonGenerator jgen, Node node, boolean isAuthorized) throws IOException {
        if (isAuthorized) {
            jgen.writeStringField("category", node.getServiceType().toString());  // necessary for go.js
            jgen.writeStringField("serviceType", node.getServiceType().toString());
            jgen.writeStringField("serviceTypeCode", Short.toString(node.getServiceType().getCode()));
        } else {
            jgen.writeStringField("category", ServiceType.UNAUTHORIZED.toString());  // necessary for go.js
            jgen.writeStringField("serviceType", ServiceType.UNAUTHORIZED.toString());
            jgen.writeStringField("serviceTypeCode", Short.toString(ServiceType.UNAUTHORIZED.getCode()));
        }

        jgen.writeBooleanField("isWas", node.getServiceType().isWas());  // for go.js
        jgen.writeBooleanField("isQueue", node.getServiceType().isQueue());
    }

    private void writeHistogram(JsonGenerator jgen, Node node, boolean isAuthorized) throws IOException {
        if (isAuthorized == false) {
            return;
        }
        
        final ServiceType serviceType = node.getServiceType();
        final NodeHistogram nodeHistogram = node.getNodeHistogram();
        // FIXME isn't this all ServiceTypes that can be a node?
        if (serviceType.isWas() || serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isUser() || serviceType.isQueue()) {
            Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
            if (applicationHistogram == null) {
                writeEmptyObject(jgen, "histogram");
                jgen.writeBooleanField("hasAlert", false);  // for go.js
            } else {
                jgen.writeObjectField("histogram", applicationHistogram);
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

            Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
            
            if(agentHistogramMap == null) {
                writeEmptyObject(jgen, "agentHistogram");
            } else {
                jgen.writeObjectField("agentHistogram", agentHistogramMap);
            }
        } else {
            jgen.writeBooleanField("hasAlert", false);  // for go.js
        }
        // FIXME isn't this all ServiceTypes that can be a node?
        if (serviceType.isWas() || serviceType.isUser() || serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isQueue()) {
            List<ResponseTimeViewModel> applicationTimeSeriesHistogram = nodeHistogram.getApplicationTimeHistogram();
            if (applicationTimeSeriesHistogram == null) {
                writeEmptyArray(jgen, "timeSeriesHistogram");
            } else {
                jgen.writeObjectField("timeSeriesHistogram", applicationTimeSeriesHistogram);
            }
            AgentResponseTimeViewModelList agentTimeSeriesHistogram = nodeHistogram.getAgentTimeHistogram();
            jgen.writeObject(agentTimeSeriesHistogram);
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
    
    private boolean check(Application application) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(application)) {
            return false;
        }
        return true;
    }


}
