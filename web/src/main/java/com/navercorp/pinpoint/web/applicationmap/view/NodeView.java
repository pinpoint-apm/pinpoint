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
import com.navercorp.pinpoint.common.server.util.json.JacksonWriterUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.HistogramAggregator;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.applicationmap.servicemap.NodeViewEntry;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.jackson.JacksonComponent;

import java.util.Map;
import java.util.Objects;

public class NodeView implements NodeViewEntry {
    private final Node node;

    private final ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView;
    private final ApplicationApdexScoreSlotView applicationApdexScoreSlotView;

    private final ServerListNodeView serverListNodeView;
    private final AgentHistogramNodeView agentHistogramNodeView;
    private final AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView;
    private final boolean serviceMap;

    public NodeView(Node node,
                    ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView,
                    ApplicationApdexScoreSlotView applicationApdexScoreSlotView, ServerListNodeView serverListNodeView,
                    AgentHistogramNodeView agentHistogramNodeView,
                    AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView,
                    boolean serviceMap) {
        this.node = Objects.requireNonNull(node, "node");

        this.applicationTimeSeriesHistogramNodeView = Objects.requireNonNull(applicationTimeSeriesHistogramNodeView, "applicationTimeSeriesHistogramNodeView");
        this.applicationApdexScoreSlotView = Objects.requireNonNull(applicationApdexScoreSlotView, "applicationApdexScoreSlotView");
        this.serverListNodeView = Objects.requireNonNull(serverListNodeView, "serverListView");
        this.agentHistogramNodeView = Objects.requireNonNull(agentHistogramNodeView, "agentHistogramView");
        this.agentTimeSeriesHistogramNodeView = Objects.requireNonNull(agentTimeSeriesHistogramNodeView, "agentTimeSeriesHistogramView");

        this.serviceMap = serviceMap;
    }

    public Node getNode() {
        return node;
    }

    public ApplicationTimeSeriesHistogramNodeView getApplicationTimeSeriesHistogramNodeView() {
        return applicationTimeSeriesHistogramNodeView;
    }

    public ApplicationApdexScoreSlotView getApplicationApdexScoreSlotView() {
        return applicationApdexScoreSlotView;
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


    @JacksonComponent
    public static class NodeViewSerializer extends ValueSerializer<NodeView> {
        private final AlertViewService alertViewService;

        private final AgentServerGroupListWriter agentServerGroupListWriter;

        public NodeViewSerializer(AlertViewService alertViewService, AgentServerGroupListWriter agentServerGroupListWriter) {
            this.alertViewService = Objects.requireNonNull(alertViewService, "alertService");
            this.agentServerGroupListWriter = Objects.requireNonNull(agentServerGroupListWriter, "agentServerGroupListWriter");
        }

        @Override
        public void serialize(NodeView nodeView, JsonGenerator jgen, SerializationContext provider) {
            Node node = nodeView.getNode();

            jgen.writeStartObject();

            if (nodeView.serviceMap) {
                jgen.writePOJOProperty("key", node.getServiceNodeName().toString());
                jgen.writeStringProperty("type", "app");
            } else {
                jgen.writePOJOProperty("key", node.getNodeName());
            }

            jgen.writeStringProperty("nodeKey", node.getNodeKey());

            jgen.writePOJOProperty("serviceKey", node.getServiceNodeName().toString());

            Application application = node.getApplication();
            jgen.writeStringProperty("serviceName", application.getService().getServiceName());
            jgen.writeStringProperty("applicationName", node.getApplicationTextName());

            final ServiceType serviceType = node.getServiceType();

            jgen.writeStringProperty("serviceType", serviceType.toString());
            jgen.writeNumberProperty("serviceTypeCode", serviceType.getCode());

            jgen.writeStringProperty("nodeCategory", serviceType.getCategory().nodeCategory().toString());
            jgen.writeBooleanProperty("isQueue", serviceType.isQueue());

            jgen.writeBooleanProperty("isAuthorized", node.isAuthorized());

            jgen.writePOJOProperty("apdex", node.getApdexScore());

            writeHistogram(nodeView, jgen, provider);
            writeServerGroupList(nodeView, jgen);

            jgen.writeEndObject();
        }


        private void writeServerGroupList(NodeView nodeView, JsonGenerator jgen) {
            final Node node = nodeView.getNode();

            if (node.getServiceType().isUnknown()) {
                writeAgentCount(0, 0, jgen);

                JacksonWriterUtils.writeEmptyArray(jgen, "agents");

                ServerListNodeView detailedServerListNodeView = nodeView.getServerListView();
                detailedServerListNodeView.writeServerList(nodeView, jgen);
            } else {
                ServerGroupList serverGroupList = node.getServerGroupList();

                writeAgentCount(serverGroupList.getInstanceCount(), getInstanceErrorCount(node), jgen);

                agentServerGroupListWriter.write("agents", serverGroupList, jgen);

                ServerListNodeView serverListNodeView = nodeView.getServerListView();
                serverListNodeView.writeServerList(nodeView, jgen);
            }
        }

        private void writeAgentCount(int instanceCount, long instanceErrorCount, JsonGenerator jgen) {
            jgen.writeNumberProperty("instanceCount", instanceCount);
            jgen.writeNumberProperty("instanceErrorCount", instanceErrorCount);
        }


        private long getInstanceErrorCount(Node node) {
            final NodeHistogram nodeHistogram = node.getNodeHistogram();
            if (nodeHistogram == null) {
                return 0;
            }

            final Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
            if (MapUtils.isEmpty(agentHistogramMap)) {
                return 0;
            }

            return HistogramAggregator.countErrorInstances(agentHistogramMap.values());
        }

        private void writeHistogram(NodeView nodeView, JsonGenerator jgen, SerializationContext provider) {
            final Node node = nodeView.getNode();
            final ServiceType serviceType = node.getServiceType();
            final NodeHistogram nodeHistogram = node.getNodeHistogram();

            // FIXME isn't this all ServiceTypes that can be a node?
            final boolean nodeServiceType = isNodeServiceType(serviceType);
            if (nodeServiceType) {
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                if (applicationHistogram == null) {
                    jgen.writeBooleanProperty("hasAlert", false);
                } else {
                    jgen.writeNumberProperty("totalCount", applicationHistogram.getTotalCount());
                    jgen.writeNumberProperty("errorCount", applicationHistogram.getTotalErrorCount());
                    jgen.writeNumberProperty("slowCount", applicationHistogram.getSlowCount());

                    jgen.writeBooleanProperty("hasAlert", alertViewService.hasAlert(applicationHistogram));
                }

                ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(applicationHistogram);
                jgen.writePOJOProperty(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);
                if (applicationHistogram == null) {
                    JacksonWriterUtils.writeEmptyObject(jgen, "histogram");
                } else {
                    jgen.writePOJOProperty("histogram", applicationHistogram);
                }

                // agent histogram
                AgentHistogramNodeView agentHistogramNodeView = nodeView.getAgentHistogramView();
                agentHistogramNodeView.writeAgentHistogram(nodeView, jgen);
            } else {
                jgen.writeBooleanProperty("hasAlert", false);
            }

            // time histogram
            // FIXME isn't this all ServiceTypes that can be a node?
            if (nodeServiceType) {

                ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView = nodeView.getApplicationTimeSeriesHistogramNodeView();
                applicationTimeSeriesHistogramNodeView.writeTimeSeriesHistogram(nodeView, jgen);

                // time series apdex score with max 24 slots
                ApplicationApdexScoreSlotView applicationApdexScoreSlotView = nodeView.getApplicationApdexScoreSlotView();
                applicationApdexScoreSlotView.writeApdexScoreSlot(nodeView, jgen);

                AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView = nodeView.getAgentTimeSeriesHistogramView();
                agentTimeSeriesHistogramNodeView.writeAgentTimeSeriesHistogram(nodeView, jgen);
            }
        }

        private boolean isNodeServiceType(ServiceType serviceType) {
            return serviceType.isWas() || serviceType.isTerminal() || serviceType.isUser() || serviceType.isUnknown() || serviceType.isQueue() || serviceType.isAlias();
        }

    }
}
