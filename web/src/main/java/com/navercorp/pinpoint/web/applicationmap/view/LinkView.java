package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkViews;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonSerialize(using = LinkView.LinkViewSerializer.class)
public class LinkView {
    private final Link link;
    private final Class<?> activeView;
    private final TimeHistogramFormat format;

    public LinkView(Link link, Class<?> activeView, TimeHistogramFormat format) {
        this.link = Objects.requireNonNull(link, "link");
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.format = Objects.requireNonNull(format, "format");
    }

    private Link getLink() {
        return link;
    }

    public Class<?> getActiveView() {
        return activeView;
    }

    private TimeHistogramFormat getFormat() {
        return format;
    }

    public static class LinkViewSerializer extends JsonSerializer<LinkView> {

        @Override
        public void serialize(LinkView linkView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            Link link = linkView.getLink();
            jgen.writeStartObject();

            jgen.writeObjectField("key", link.getLinkName());  // for servermap

            jgen.writeObjectField("from", link.getFrom().getNodeName());  // necessary for go.js
            jgen.writeObjectField("to", link.getTo().getNodeName()); // necessary for go.js

            // for FilterWizard. from, to agent mapping data
            writeAgentId("fromAgent", link.getFrom(), jgen);
            writeAgentId("toAgent", link.getTo(), jgen);

            //for FilterWizard. show agent name as tooltip on instance
            writeAgentIdNameMap("fromAgentIdNameMap", link.getFrom(), jgen);
            writeAgentIdNameMap("toAgentIdNameMap", link.getTo(), jgen);

            writeSimpleNode("sourceInfo", link.getFrom(), jgen);
            writeSimpleNode("targetInfo", link.getTo(), jgen);

            Application filterApplication = link.getFilterApplication();
            jgen.writeStringField("filterApplicationName", filterApplication.getName());
            jgen.writeNumberField("filterApplicationServiceTypeCode", filterApplication.getServiceTypeCode());
            jgen.writeStringField("filterApplicationServiceTypeName", filterApplication.getServiceType().getName());
            if (link.isWasToWasLink()) {
                writeWasToWasTargetRpcList(link, jgen);
            }

            Histogram histogram = link.getHistogram();
            jgen.writeNumberField("totalCount", histogram.getTotalCount()); // for go.js
            jgen.writeNumberField("errorCount", histogram.getTotalErrorCount());
            jgen.writeNumberField("slowCount", histogram.getSlowCount());

            ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(histogram);
            jgen.writeObjectField(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);


            jgen.writeObjectField("histogram", histogram);
            final Class<?> activeView = linkView.getActiveView();
            //time histogram
            if (!LinkViews.Simplified.inView(activeView)){
                writeTimeSeriesHistogram(link, linkView.getFormat(), jgen);
            }

            //agent histogram
            if (LinkViews.Detailed.inView(activeView)) {
                // data showing how agents call each of their respective links
                writeAgentHistogram("sourceHistogram", link.getSourceList(), jgen);
                writeAgentHistogram("targetHistogram", link.getTargetList(), jgen);
                writeSourceAgentTimeSeriesHistogram(linkView, jgen);
                writeAgentResponseStatistics("sourceResponseStatistics", link.getSourceList(), jgen);
                writeAgentResponseStatistics("targetResponseStatistics", link.getTargetList(), jgen);
            }

//        String state = link.getLinkState();
//        jgen.writeStringField("state", state); // for go.js
            jgen.writeBooleanField("hasAlert", link.getLinkAlert()); // for go.js

            jgen.writeEndObject();
        }

        private void writeAgentId(String fieldName, Node node, JsonGenerator jgen) throws IOException {
            if (node.getServiceType().isWas()) {
                jgen.writeFieldName(fieldName);
                jgen.writeStartArray();
                ServerGroupList serverGroupList = node.getServerGroupList();
                if (serverGroupList != null) {
                    for (String agentId : serverGroupList.getAgentIdList()) {
                        jgen.writeObject(agentId);
                    }
                }
                jgen.writeEndArray();
            }
        }

        private void writeAgentIdNameMap(String fieldName, Node node, JsonGenerator jgen) throws IOException {
            if (node.getServiceType().isWas()) {
                jgen.writeFieldName(fieldName);
                jgen.writeStartObject();
                ServerGroupList serverGroupList = node.getServerGroupList();
                if (serverGroupList != null) {
                    for (Map.Entry<String, String> entry : serverGroupList.getAgentIdNameMap().entrySet()) {
                        jgen.writeStringField(entry.getKey(), entry.getValue());
                    }
                }
                jgen.writeEndObject();
            }
        }

        private void writeWasToWasTargetRpcList(Link link, JsonGenerator jgen) throws IOException {
            // write additional information to be used for filtering failed WAS -> WAS call events.
            jgen.writeFieldName("filterTargetRpcList");
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

        private void writeTimeSeriesHistogram(Link link, TimeHistogramFormat format, JsonGenerator jgen) throws IOException {
            List<TimeHistogramViewModel> sourceApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram(format);
            jgen.writeFieldName("timeSeriesHistogram");
            jgen.writeObject(sourceApplicationTimeSeriesHistogram);
        }

        private void writeAgentResponseStatistics(String fieldName, AgentHistogramList agentHistogramList, JsonGenerator jgen) throws IOException {
            jgen.writeFieldName(fieldName);
            jgen.writeStartObject();
            for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
                jgen.writeFieldName(agentHistogram.getId());
                jgen.writeObject(ResponseTimeStatics.fromHistogram(agentHistogram.getHistogram()));
            }
            jgen.writeEndObject();
        }

        private void writeAgentHistogram(String fieldName, AgentHistogramList agentHistogramList, JsonGenerator jgen) throws IOException {
            jgen.writeFieldName(fieldName);
            jgen.writeStartObject();
            for (AgentHistogram agentHistogram : agentHistogramList.getAgentHistogramList()) {
                jgen.writeFieldName(agentHistogram.getId());
                jgen.writeObject(agentHistogram.getHistogram());
            }
            jgen.writeEndObject();
        }

        private void writeSourceAgentTimeSeriesHistogram(LinkView linkView, JsonGenerator jgen) throws IOException {
            Link link = linkView.getLink();
            TimeHistogramFormat format = linkView.getFormat();
            JsonFields<AgentNameView, List<TimeHistogramViewModel>> sourceAgentTimeSeriesHistogram = link.getSourceAgentTimeSeriesHistogram(format);
//        sourceAgentTimeSeriesHistogram.setFieldName("sourceTimeSeriesHistogram");
            jgen.writeFieldName("sourceTimeSeriesHistogram");
            jgen.writeObject(sourceAgentTimeSeriesHistogram);
        }

        private void writeSimpleNode(String fieldName, Node node, JsonGenerator jgen) throws IOException {
            jgen.writeFieldName(fieldName);

            jgen.writeStartObject();
            Application application = node.getApplication();
            jgen.writeStringField("applicationName", application.getName());
            jgen.writeStringField("serviceType", application.getServiceType().toString());
            jgen.writeNumberField("serviceTypeCode", application.getServiceTypeCode());
            jgen.writeBooleanField("isWas", application.getServiceType().isWas());
            jgen.writeEndObject();
        }
    }
}
