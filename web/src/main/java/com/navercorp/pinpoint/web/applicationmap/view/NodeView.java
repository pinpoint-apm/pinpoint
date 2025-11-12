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
import com.navercorp.pinpoint.common.server.util.json.JacksonWriterUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class NodeView {
    private final Node node;

    private final ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView;

    private final ServerListNodeView serverListNodeView;
    private final AgentHistogramNodeView agentHistogramNodeView;
    private final AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView;

    public NodeView(Node node,
                    ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView,
                    ServerListNodeView serverListNodeView,
                    AgentHistogramNodeView agentHistogramNodeView,
                    AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView) {
        this.node = Objects.requireNonNull(node, "node");

        this.applicationTimeSeriesHistogramNodeView = Objects.requireNonNull(applicationTimeSeriesHistogramNodeView, "applicationTimeSeriesHistogramNodeView");
        this.serverListNodeView = Objects.requireNonNull(serverListNodeView, "serverListView");
        this.agentHistogramNodeView = Objects.requireNonNull(agentHistogramNodeView, "agentHistogramView");
        this.agentTimeSeriesHistogramNodeView = Objects.requireNonNull(agentTimeSeriesHistogramNodeView, "agentTimeSeriesHistogramView");
    }

    public Node getNode() {
        return node;
    }

    public ApplicationTimeSeriesHistogramNodeView getApplicationTimeSeriesHistogramNodeView() {
        return applicationTimeSeriesHistogramNodeView;
    }

    public ServerListNodeView getServerListView() {
        return serverListNodeView;
    }

    public AgentHistogramNodeView getAgentHistogramView() {
        return agentHistogramNodeView;
    }

    public AgentTimeSeriesHistogramNodeView getAgentTimeSeriesHistogramView() {
        return agentTimeSeriesHistogramNodeView;
    }


    @JsonComponent
    public static class NodeViewSerializer extends JsonSerializer<NodeView> {
        private final AlertViewService alertViewService;

        private final AgentServerGroupListWriter agentServerGroupListWriter;

        public NodeViewSerializer(AlertViewService alertViewService, AgentServerGroupListWriter agentServerGroupListWriter) {
            this.alertViewService = Objects.requireNonNull(alertViewService, "alertService");
            this.agentServerGroupListWriter = Objects.requireNonNull(agentServerGroupListWriter, "agentServerGroupListWriter");
        }

        @Override
        public void serialize(NodeView nodeView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            Node node = nodeView.getNode();

            jgen.writeStartObject();
//        jgen.writeStringField("id", node.getNodeName()); serverInstanceList
            jgen.writeObjectField("key", node.getNodeName()); // necessary for go.js
            jgen.writeStringField("nodeKey", node.getNodeKey());

            jgen.writeStringField("applicationName", node.getApplicationTextName()); // for go.js

            final ServiceType serviceType = node.getServiceType();

            jgen.writeStringField("serviceType", serviceType.toString());
            jgen.writeNumberField("serviceTypeCode", serviceType.getCode());

            jgen.writeStringField("nodeCategory", serviceType.getCategory().nodeCategory().toString());
            jgen.writeBooleanField("isQueue", serviceType.isQueue());

            jgen.writeBooleanField("isAuthorized", node.isAuthorized());

            jgen.writeObjectField("apdex", node.getApdexScore());

            writeHistogram(nodeView, jgen, provider);
            writeServerGroupList(nodeView, jgen);

            jgen.writeEndObject();
        }


        private void writeServerGroupList(NodeView nodeView, JsonGenerator jgen) throws IOException {
            final Node node = nodeView.getNode();
            ServerGroupList serverGroupList = node.getServerGroupList();

            if (node.getServiceType().isUnknown()) {
                writeAgentCount(0, 0, jgen);

                JacksonWriterUtils.writeEmptyArray(jgen, "agents");

                ServerListNodeView detailedServerListNodeView = nodeView.getServerListView();
                detailedServerListNodeView.writeServerList(nodeView, jgen);
            } else {
                writeAgentCount(serverGroupList.getInstanceCount(), getInstanceErrorCount(node), jgen);

                agentServerGroupListWriter.write("agents", serverGroupList, jgen);

                ServerListNodeView serverListNodeView = nodeView.getServerListView();
                serverListNodeView.writeServerList(nodeView, jgen);
            }
        }

        private void writeAgentCount(int instanceCount, long instanceErrorCount, JsonGenerator jgen) throws IOException {
            jgen.writeNumberField("instanceCount", instanceCount);
            jgen.writeNumberField("instanceErrorCount", instanceErrorCount);
        }


        private long getInstanceErrorCount(Node node) {
            final NodeHistogram nodeHistogram = node.getNodeHistogram();
            if (nodeHistogram == null) {
                return 0;
            }

            final Map<String, Histogram> agentHistogramMap = node.getNodeHistogram().getAgentHistogramMap();
            if (agentHistogramMap == null) {
                return 0;
            }

            return agentHistogramMap.values().stream()
                    .filter(agentHistogram -> agentHistogram.getTotalErrorCount() > 0)
                    .count();
        }

        private void writeHistogram(NodeView nodeView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            final Node node = nodeView.getNode();
            final ServiceType serviceType = node.getServiceType();
            final NodeHistogram nodeHistogram = node.getNodeHistogram();

            // FIXME isn't this all ServiceTypes that can be a node?
            final boolean nodeServiceType = isNodeServiceType(serviceType);
            if (nodeServiceType) {
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                if (applicationHistogram == null) {
                    jgen.writeBooleanField("hasAlert", false);  // for go.js
                } else {
                    jgen.writeNumberField("totalCount", applicationHistogram.getTotalCount()); // for go.js
                    jgen.writeNumberField("errorCount", applicationHistogram.getTotalErrorCount());
                    jgen.writeNumberField("slowCount", applicationHistogram.getSlowCount());

                    jgen.writeBooleanField("hasAlert", alertViewService.hasAlert(applicationHistogram));  // for go.js
                }

                ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(applicationHistogram);
                jgen.writeObjectField(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);
                if (applicationHistogram == null) {
                    JacksonWriterUtils.writeEmptyObject(jgen, "histogram");
                } else {
                    jgen.writeObjectField("histogram", applicationHistogram);
                }

                // agent histogram
                AgentHistogramNodeView agentHistogramNodeView = nodeView.getAgentHistogramView();
                agentHistogramNodeView.writeAgentHistogram(nodeView, jgen);
            } else {
                jgen.writeBooleanField("hasAlert", false);  // for go.js
            }

            // time histogram
            // FIXME isn't this all ServiceTypes that can be a node?
            if (nodeServiceType) {

                ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView = nodeView.getApplicationTimeSeriesHistogramNodeView();
                applicationTimeSeriesHistogramNodeView.writeTimeSeriesHistogram(nodeView, jgen);

                AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView = nodeView.getAgentTimeSeriesHistogramView();
                agentTimeSeriesHistogramNodeView.writeAgentTimeSeriesHistogram(nodeView, jgen);
            }
        }

        private boolean isNodeServiceType(ServiceType serviceType) {
            return serviceType.isWas() || serviceType.isTerminal() || serviceType.isUser() || serviceType.isUnknown() || serviceType.isQueue() || serviceType.isAlias();
        }

    }
}
