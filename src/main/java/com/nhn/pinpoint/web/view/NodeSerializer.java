package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.Node;
import com.nhn.pinpoint.web.applicationmap.ServerInstanceList;
import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;
import com.nhn.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class NodeSerializer extends JsonSerializer<Node>  {
    @Override
    public void serialize(Node node, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
//        jgen.writeStringField("id", node.getNodeName());
        jgen.writeStringField("key", node.getNodeName()); // necessary for go.js

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
        jgen.writeBooleanField("isFiltered", true);  // for go.js
        jgen.writeBooleanField("hasAlert", true);  // for go.js

        writeHistogram(jgen, node);
        if (node.getServiceType().isUnknown()) {
            writeEmptyObject(jgen, "serverList");
        } else {
            final ServerInstanceList serverInstanceList = node.getServerInstanceList();
            if (serverInstanceList != null) {
                jgen.writeObjectField("serverList", serverInstanceList);
            } else {
                writeEmptyObject(jgen, "serverList");
            }
        }

        jgen.writeEndObject();
    }

    private void writeHistogram(JsonGenerator jgen, Node node) throws IOException {
        final ServiceType serviceType = node.getServiceType();
        final NodeHistogram nodeHistogram = node.getNodeHistogram();
        if (serviceType.isWas() || serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isUser()) {
            Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
            if (applicationHistogram == null) {
                writeEmptyObject(jgen, "histogram");
            } else {
                jgen.writeObjectField("histogram", applicationHistogram);
                jgen.writeNumberField("totalCount", applicationHistogram.getTotalCount()); // for go.js
                jgen.writeNumberField("errorCount", applicationHistogram.getErrorCount());
                jgen.writeNumberField("slowCount", applicationHistogram.getSlowCount());
            }

            Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
            if(agentHistogramMap == null) {
                writeEmptyObject(jgen, "agentHistogram");
            } else {
                jgen.writeObjectField("agentHistogram", agentHistogramMap);
            }
        }
        if (serviceType.isWas() || serviceType.isUser() || serviceType.isTerminal() || serviceType.isUnknown()) {
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


}
