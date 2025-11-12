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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public class LinkView {
    private final Link link;

    private final ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView;
    private final AgentLinkView agentLinkView;

    public LinkView(Link link,
                    ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView,
                    AgentLinkView agentLinkView) {
        this.link = Objects.requireNonNull(link, "link");
        this.applicationTimeSeriesHistogramLinkView = Objects.requireNonNull(applicationTimeSeriesHistogramLinkView, "applicationTimeSeriesHistogramLinkView");
        this.agentLinkView = Objects.requireNonNull(agentLinkView, "agentLinkView");
    }

    public Link getLink() {
        return link;
    }

    public AgentLinkView getAgentLinkView() {
        return agentLinkView;
    }

    public ApplicationTimeSeriesHistogramLinkView getApplicationTimeSeriesHistogramLinkView() {
        return applicationTimeSeriesHistogramLinkView;
    }

    @JsonComponent
    public static class LinkViewSerializer extends JsonSerializer<LinkView> {

        private final AlertViewService alertViewService;

        private final AgentServerGroupListWriter agentServerGroupListWriter;

        public LinkViewSerializer(AlertViewService alertViewService, AgentServerGroupListWriter agentServerGroupListWriter) {
            this.alertViewService = Objects.requireNonNull(alertViewService, "alertViewService");
            this.agentServerGroupListWriter = Objects.requireNonNull(agentServerGroupListWriter, "agentServerGroupListWriter");
        }

        @Override
        public void serialize(LinkView linkView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            Link link = linkView.getLink();
            jgen.writeStartObject();

            jgen.writeObjectField("key", link.getLinkName());  // for servermap
            jgen.writeStringField("linkKey", link.getLinkNameKey());

            jgen.writeObjectField("from", link.getFrom().getNodeName());  // necessary for go.js
            jgen.writeObjectField("to", link.getTo().getNodeName()); // necessary for go.js

            // for FilterWizard, to agent mapping data
            writeAgents("fromAgents", link.getFrom(), jgen);
            writeAgents("toAgents", link.getTo(), jgen);

            writeSimpleNode("sourceInfo", link.getFrom(), jgen);
            writeSimpleNode("targetInfo", link.getTo(), jgen);

            writerFilterHint(link, jgen);

            Histogram histogram = link.getHistogram();
            jgen.writeNumberField("totalCount", histogram.getTotalCount()); // for go.js
            jgen.writeNumberField("errorCount", histogram.getTotalErrorCount());
            jgen.writeNumberField("slowCount", histogram.getSlowCount());

            ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(histogram);
            jgen.writeObjectField(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);


            jgen.writeObjectField("histogram", histogram);
            // time histogram
            ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView = linkView.getApplicationTimeSeriesHistogramLinkView();
            applicationTimeSeriesHistogramLinkView.writeTimeSeriesHistogram(linkView, jgen);


            AgentLinkView agentLinkView = linkView.getAgentLinkView();
            agentLinkView.writeAgentLink(linkView, jgen);

//        String state = link.getLinkState();
//        jgen.writeStringField("state", state); // for go.js
            boolean alert = alertViewService.hasAlert(link.getHistogram());
            jgen.writeBooleanField("hasAlert", alert); // for go.js

            jgen.writeEndObject();
        }

        private void writerFilterHint(Link link, JsonGenerator jgen) throws IOException {
            Application filterApplication = link.getFilterApplication();
            jgen.writeFieldName("filter");
            jgen.writeStartObject();
            jgen.writeStringField("applicationName", filterApplication.getName());
            ServiceType serviceType = filterApplication.getServiceType();
            jgen.writeNumberField("serviceTypeCode", serviceType.getCode());
            jgen.writeStringField("serviceTypeName", serviceType.getName());
            if (link.isWasToWasLink()) {
                writeWasToWasTargetRpcList("outRpcList", link, jgen);
            }
            jgen.writeEndObject();
        }

        private void writeAgents(String fieldName, Node node, JsonGenerator jgen) throws IOException {
            if (node.getServiceType().isWas()) {
                ServerGroupList serverGroups = node.getServerGroupList();
                agentServerGroupListWriter.write(fieldName, serverGroups, jgen);
            }
        }

        private void writeWasToWasTargetRpcList(String fieldName, Link link, JsonGenerator jgen) throws IOException {
            // write additional information to be used for filtering failed WAS -> WAS call events.
            jgen.writeFieldName(fieldName);
            jgen.writeStartArray();
            Collection<Application> sourceLinkTargetAgentList = link.getSourceLinkTargetAgentList();
            for (Application application : sourceLinkTargetAgentList) {
                jgen.writeStartObject();
                jgen.writeStringField("rpc", application.getName());
                jgen.writeNumberField("rpcServiceTypeCode", application.getServiceTypeCode());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();

        }

        private void writeSimpleNode(String fieldName, Node node, JsonGenerator jgen) throws IOException {
            jgen.writeFieldName(fieldName);

            jgen.writeStartObject();
            Application application = node.getApplication();
            jgen.writeStringField("applicationName", application.getName());
            ServiceType serviceType = application.getServiceType();
            jgen.writeStringField("serviceType", serviceType.toString());
            jgen.writeNumberField("serviceTypeCode", serviceType.getCode());
            jgen.writeStringField("nodeCategory", serviceType.getCategory().nodeCategory().toString());

            jgen.writeEndObject();
        }
    }
}
