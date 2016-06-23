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
import com.navercorp.pinpoint.web.applicationmap.Link;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.vo.Application;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author emeroad
 * @author netspider
 */
public class LinkSerializer extends JsonSerializer<Link> {
    
    @Autowired(required=false)
    private ServerMapDataFilter serverMapDataFilter;
    
    @Override
    public void serialize(Link link, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        final boolean isAuthFromApp = check(link.getFrom().getApplication());
        final boolean isAuthToApp = check(link.getTo().getApplication());
        
        jgen.writeStartObject();

        jgen.writeStringField("key", link.getLinkName(isAuthFromApp, isAuthToApp));  // for servermap

        jgen.writeStringField("from", link.getFrom().getNodeName(isAuthFromApp));  // necessary for go.js
        jgen.writeStringField("to", link.getTo().getNodeName(isAuthToApp)); // necessary for go.js

        // for FilterWizard. from, to agent mapping data
        writeAgentId("fromAgent", link.getFrom(), jgen, isAuthFromApp);
        writeAgentId("toAgent", link.getTo(), jgen, isAuthToApp);

        writeSimpleNode("sourceInfo", link.getFrom(), jgen, isAuthFromApp);
        writeSimpleNode("targetInfo", link.getTo(), jgen, isAuthToApp);

        writeFilterApplicationInfo(link, jgen);
        
        if (link.isWasToWasLink()) {
            writeWasToWasTargetRpcList(link, jgen, isAuthToApp);
        }

        Histogram histogram = link.getHistogram();
        jgen.writeNumberField("totalCount", histogram.getTotalCount()); // for go.js
        jgen.writeNumberField("errorCount", histogram.getTotalErrorCount());
        jgen.writeNumberField("slowCount", histogram.getSlowCount());

        jgen.writeObjectField("histogram", histogram);

        // data showing how agents call each of their respective links
        writeAgentHistogram("sourceHistogram", link.getSourceList(), jgen, isAuthFromApp);
        writeAgentHistogram("targetHistogram", link.getTargetList(), jgen, isAuthToApp);

        writeTimeSeriesHistogram(link, jgen);
        writeSourceAgentTimeSeriesHistogram(link, jgen, isAuthFromApp);


//        String state = link.getLinkState();
//        jgen.writeStringField("state", state); // for go.js
        jgen.writeBooleanField("hasAlert", link.getLinkAlert()); // for go.js

        jgen.writeEndObject();
    }
    
    private void writeFilterApplicationInfo(Link link, JsonGenerator jgen) throws IOException {
        Application application = link.getFilterApplication();
        jgen.writeStringField("filterApplicationName", application.getName());
        if (check(application)) {
            jgen.writeNumberField("filterApplicationServiceTypeCode", application.getServiceTypeCode());
            jgen.writeStringField("filterApplicationServiceTypeName", application.getServiceType().getName());
        } else {
            jgen.writeNumberField("filterApplicationServiceTypeCode", ServiceType.UNAUTHORIZED.getCode());
            jgen.writeStringField("filterApplicationServiceTypeName", ServiceType.UNAUTHORIZED.getName());
        }
    }

    private boolean check(Application application) {
        if (serverMapDataFilter != null && serverMapDataFilter.filter(application)) {
            return false;
        }
        return true;
    }

    private void writeAgentId(String fieldName, Node node, JsonGenerator jgen, boolean isAuthorized) throws IOException {
        if (node.getServiceType().isWas()) {
            jgen.writeFieldName(fieldName);
            jgen.writeStartArray();
            if (isAuthorized) {
                ServerInstanceList serverInstanceList = node.getServerInstanceList();
                if (serverInstanceList!= null) {
                    for (String agentId : serverInstanceList.getAgentIdList()) {
                        jgen.writeObject(agentId);
                    }
                }
            } else {
                jgen.writeString("UNKNOWN_AGENT");
            }
            jgen.writeEndArray();
        }
    }

    private void writeWasToWasTargetRpcList(Link link, JsonGenerator jgen, boolean isAuthorized) throws IOException {
        // write additional information to be used for filtering failed WAS -> WAS call events.
        jgen.writeFieldName("filterTargetRpcList");
        jgen.writeStartArray();
        if (isAuthorized) {
            Collection<Application> sourceLinkTargetAgentList = link.getSourceLinkTargetAgentList();
            for (Application application : sourceLinkTargetAgentList) {
                jgen.writeStartObject();
                jgen.writeStringField("rpc", application.getName());
                if (check(application)) {
                    jgen.writeNumberField("rpcServiceTypeCode", application.getServiceTypeCode());
                }
                else {
                    
                }
                jgen.writeEndObject();
            }
        }
        jgen.writeEndArray();

    }



    private void writeTimeSeriesHistogram(Link link, JsonGenerator jgen) throws IOException {
        List<ResponseTimeViewModel> sourceApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram();

        jgen.writeFieldName("timeSeriesHistogram");
        jgen.writeObject(sourceApplicationTimeSeriesHistogram);
    }


    private void writeAgentHistogram(String fieldName, AgentHistogramList agentHistogramList, JsonGenerator jgen, boolean isAuthorized) throws IOException {
        jgen.writeFieldName(fieldName);
        jgen.writeStartObject();
        if (isAuthorized) {
            for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
                jgen.writeFieldName(agentHistogram.getId());
                jgen.writeObject(agentHistogram.getHistogram());
            }
        } else {
            AgentHistogram mergeHistogram = new AgentHistogram(new Application("UNKONWN_AGENT", ServiceType.UNAUTHORIZED));
            for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
                mergeHistogram.addTimeHistogram(agentHistogram.getTimeHistogram());
            }
                jgen.writeFieldName(mergeHistogram.getId());
                jgen.writeObject(mergeHistogram.getHistogram());
        }
        jgen.writeEndObject();
    }

    private void writeSourceAgentTimeSeriesHistogram(Link link, JsonGenerator jgen, boolean isAuthorized) throws IOException {
        AgentResponseTimeViewModelList sourceAgentTimeSeriesHistogram = link.getSourceAgentTimeSeriesHistogram();
        sourceAgentTimeSeriesHistogram.setFieldName("sourceTimeSeriesHistogram");
        if (isAuthorized) {
            jgen.writeObject(sourceAgentTimeSeriesHistogram);
        } else {
            jgen.writeFieldName("sourceTimeSeriesHistogram");
            jgen.writeStartObject();
            jgen.writeEndObject();
        }
    }

    private void writeSimpleNode(String fieldName, Node node, JsonGenerator jgen, boolean isAuthorized) throws IOException {
        jgen.writeFieldName(fieldName);

        jgen.writeStartObject();
        Application application = node.getApplication();
        jgen.writeStringField("applicationName", application.getName());
        if (isAuthorized) {
            jgen.writeStringField("serviceType", application.getServiceType().toString());
            jgen.writeNumberField("serviceTypeCode", application.getServiceTypeCode());
        } else {
            jgen.writeStringField("serviceType", ServiceType.UNAUTHORIZED.toString());
            jgen.writeNumberField("serviceTypeCode", ServiceType.UNAUTHORIZED.getCode());
        }
        jgen.writeBooleanField("isWas", application.getServiceType().isWas());
        jgen.writeEndObject();
    }
}
