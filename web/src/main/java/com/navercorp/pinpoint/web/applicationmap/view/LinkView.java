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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.applicationmap.servicemap.LinkViewEntry;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;
import org.springframework.boot.jackson.JacksonComponent;

import java.util.Collection;
import java.util.Objects;

public class LinkView implements LinkViewEntry {
    private final Link link;

    private final ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView;
    private final AgentLinkView agentLinkView;
    private final boolean enableServiceMap;

    public LinkView(Link link,
                    ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView,
                    AgentLinkView agentLinkView,
                    boolean enableServiceMap) {
        this.link = Objects.requireNonNull(link, "link");
        this.applicationTimeSeriesHistogramLinkView = Objects.requireNonNull(applicationTimeSeriesHistogramLinkView, "applicationTimeSeriesHistogramLinkView");
        this.agentLinkView = Objects.requireNonNull(agentLinkView, "agentLinkView");
        this.enableServiceMap = enableServiceMap;
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

    @JacksonComponent
    public static class LinkViewSerializer extends ValueSerializer<LinkView> {

        private final AlertViewService alertViewService;

        private final AgentServerGroupListWriter agentServerGroupListWriter;

        public LinkViewSerializer(AlertViewService alertViewService, AgentServerGroupListWriter agentServerGroupListWriter) {
            this.alertViewService = Objects.requireNonNull(alertViewService, "alertViewService");
            this.agentServerGroupListWriter = Objects.requireNonNull(agentServerGroupListWriter, "agentServerGroupListWriter");
        }

        @Override
        public void serialize(LinkView linkView, JsonGenerator jgen, SerializationContext provider) {
            Link link = linkView.getLink();
            jgen.writeStartObject();

            if (linkView.enableServiceMap) {
                jgen.writePOJOProperty("key", link.getServiceLinkName().toString());
                jgen.writePOJOProperty("from", link.getFrom().getServiceNodeName().toString());
                jgen.writePOJOProperty("to", link.getTo().getServiceNodeName().toString());
                jgen.writeStringProperty("type", "app");
            } else {
                jgen.writePOJOProperty("key", link.getLinkName());
                jgen.writePOJOProperty("from", link.getFrom().getNodeName());
                jgen.writePOJOProperty("to", link.getTo().getNodeName());
            }

            jgen.writeStringProperty("linkKey", link.getLinkNameKey());
            jgen.writePOJOProperty("serviceKey", link.getServiceLinkName().toString());


            // for FilterWizard, to agent mapping data
            writeAgents("fromAgents", link.getFrom(), jgen);
            writeAgents("toAgents", link.getTo(), jgen);

            writeSimpleNode("sourceInfo", link.getFrom(), jgen);
            writeSimpleNode("targetInfo", link.getTo(), jgen);

            writerFilterHint(link, jgen);

            Histogram histogram = link.getHistogram();
            jgen.writeNumberProperty("totalCount", histogram.getTotalCount());
            jgen.writeNumberProperty("errorCount", histogram.getTotalErrorCount());
            jgen.writeNumberProperty("slowCount", histogram.getSlowCount());

            ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(histogram);
            jgen.writePOJOProperty(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);


            jgen.writePOJOProperty("histogram", histogram);
            // time histogram
            ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView = linkView.getApplicationTimeSeriesHistogramLinkView();
            applicationTimeSeriesHistogramLinkView.writeTimeSeriesHistogram(linkView, jgen);


            AgentLinkView agentLinkView = linkView.getAgentLinkView();
            agentLinkView.writeAgentLink(linkView, jgen);

//        String state = link.getLinkState();
//        jgen.writeStringProperty("state", state);
            boolean alert = alertViewService.hasAlert(link.getHistogram());
            jgen.writeBooleanProperty("hasAlert", alert);

            jgen.writeEndObject();
        }

        private void writerFilterHint(Link link, JsonGenerator jgen) {
            Application filterApplication = link.getFilterApplication();
            jgen.writeName("filter");
            jgen.writeStartObject();
            jgen.writeStringProperty("serviceName", filterApplication.getService().getServiceName());
            jgen.writeStringProperty("applicationName", filterApplication.getApplicationName());
            ServiceType serviceType = filterApplication.getServiceType();
            jgen.writeNumberProperty("serviceTypeCode", serviceType.getCode());
            jgen.writeStringProperty("serviceTypeName", serviceType.getName());
            if (link.isWasToWasLink()) {
                writeWasToWasTargetRpcList("outRpcList", link, jgen);
            }
            jgen.writeEndObject();
        }

        private void writeAgents(String fieldName, Node node, JsonGenerator jgen) {
            if (node.getServiceType().isWas()) {
                ServerGroupList serverGroups = node.getServerGroupList();
                agentServerGroupListWriter.write(fieldName, serverGroups, jgen);
            }
        }

        private void writeWasToWasTargetRpcList(String fieldName, Link link, JsonGenerator jgen) {
            // write additional information to be used for filtering failed WAS -> WAS call events.
            jgen.writeName(fieldName);
            jgen.writeStartArray();
            Collection<Application> sourceLinkTargetAgentList = link.getSourceLinkTargetAgentList();
            for (Application application : sourceLinkTargetAgentList) {
                jgen.writeStartObject();
                jgen.writeStringProperty("rpc", application.getApplicationName());
                jgen.writeNumberProperty("rpcServiceTypeCode", application.getServiceTypeCode());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();

        }

        private void writeSimpleNode(String fieldName, Node node, JsonGenerator jgen) {
            jgen.writeName(fieldName);

            jgen.writeStartObject();
            Application application = node.getApplication();
            jgen.writeStringProperty("serviceName", application.getService().getServiceName());
            jgen.writeStringProperty("applicationName", application.getApplicationName());
            ServiceType serviceType = application.getServiceType();
            jgen.writeStringProperty("serviceType", serviceType.toString());
            jgen.writeNumberProperty("serviceTypeCode", serviceType.getCode());
            jgen.writeStringProperty("nodeCategory", serviceType.getCategory().nodeCategory().toString());

            jgen.writeEndObject();
        }
    }
}
