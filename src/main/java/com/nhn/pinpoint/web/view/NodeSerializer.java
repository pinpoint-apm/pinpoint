package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.Node;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.vo.ResponseHistogramSummary;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

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
        jgen.writeStringField("id", node.getNodeName());
        jgen.writeStringField("key", node.getNodeName());

        jgen.writeStringField("text", node.getApplicationName());

        jgen.writeStringField("category", node.getServiceType().toString());

        final ServiceType serviceType = node.getApplication().getServiceType();
        if (serviceType.isUser()) {
            jgen.writeStringField("fig", "Ellipse");
        } else if(serviceType.isWas()) {
            jgen.writeStringField("fig", "RoundedRectangle");
        } else {
            jgen.writeStringField("fig", "Rectangle");
        }

        jgen.writeStringField("serviceTypeCode", Short.toString(serviceType.getCode()));
        jgen.writeStringField("terminal", Boolean.toString(serviceType.isTerminal()));
        jgen.writeBooleanField("isWas", serviceType.isWas());

        writeHistogram(jgen, node);
        if (node.getServiceType().isUnknown()) {
            writeEmptyObject(jgen, "serverList");
        } else {
            jgen.writeObjectField("serverList", node.getServerInstanceList());
        }

        jgen.writeEndObject();
    }

    private void writeHistogram(JsonGenerator jgen, Node node) throws IOException {
        final ServiceType serviceType = node.getServiceType();
        final ResponseHistogramSummary responseHistogramSummary = node.getResponseHistogramSummary();
        if (serviceType.isWas() || serviceType.isTerminal() || serviceType.isUnknown() || serviceType.isUser()) {
            Histogram applicationHistogram = responseHistogramSummary.getApplicationHistogram();
            if (applicationHistogram == null) {
                writeEmptyObject(jgen, "histogram");
            } else {
                jgen.writeObjectField("histogram", applicationHistogram);
            }

            Map<String, Histogram> agentHistogramMap = responseHistogramSummary.getAgentHistogramMap();
            if(agentHistogramMap == null) {
                writeEmptyObject(jgen, "agentHistogram");
            } else {
                jgen.writeObjectField("agentHistogram", agentHistogramMap);
            }
        }
        if (serviceType.isWas()) {
            List<ResponseTimeViewModel> applicationTimeSeriesHistogram = responseHistogramSummary.getApplicationTimeSeriesHistogram();
            if (applicationTimeSeriesHistogram == null) {
                writeEmptyArray(jgen, "timeSeriesHistogram");
            } else {
                jgen.writeObjectField("timeSeriesHistogram", applicationTimeSeriesHistogram);
            }

            List<AgentResponseTimeViewModel> agentTimeSeriesHistogram = responseHistogramSummary.getAgentTimeSeriesHistogram();

            if (agentTimeSeriesHistogram == null) {
                writeEmptyObject(jgen, "agentTimeSeriesHistogram");
            } else {
                jgen.writeFieldName("agentTimeSeriesHistogram");
                jgen.writeStartObject();
                for (AgentResponseTimeViewModel agentResponseTimeViewModel : agentTimeSeriesHistogram) {
                    jgen.writeObject(agentResponseTimeViewModel);
                }
                jgen.writeEndObject();
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
