package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.web.applicationmap.Link;
import com.nhn.pinpoint.web.applicationmap.Node;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;
import com.nhn.pinpoint.web.vo.Application;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public class LinkSerializer extends JsonSerializer<Link> {
    @Override
    public void serialize(Link link, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeStringField("id", link.getLinkName());

        jgen.writeStringField("from", link.getFrom().getNodeName());
        jgen.writeStringField("to", link.getTo().getNodeName());


        writeSimpleNode("sourceInfo", link.getFrom(), jgen);
        writeSimpleNode("targetInfo", link.getTo(), jgen);

        Application filterApplication = link.getFilterApplication();
        jgen.writeStringField("filterApplicationName", filterApplication.getName());
        jgen.writeNumberField("filterApplicationServiceTypeCode", filterApplication.getServiceTypeCode());

        Histogram histogram = link.getHistogram();
        jgen.writeNumberField("text", histogram.getTotalCount());
        jgen.writeNumberField("error", histogram.getErrorCount());
        jgen.writeNumberField("slow", histogram.getSlowCount());

        jgen.writeObjectField("histogram", histogram);

        // 링크별 각 agent가 어떻게 호출했는지 데이터
        writeAgentHistogram("sourceHistogram", link.getSourceList(), jgen);
        writeAgentHistogram("targetHistogram", link.getTargetList(), jgen);

        writeTimeSeriesHistogram(link, jgen);
        writeSourceAgentTimeSeriesHistogram(link, jgen);


        String state = link.getLinkState();
        jgen.writeStringField("category", state);

        jgen.writeEndObject();
    }



    private void writeTimeSeriesHistogram(Link link, JsonGenerator jgen) throws IOException {
        List<ResponseTimeViewModel> sourceApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram();

        jgen.writeFieldName("timeSeriesHistogram");
        jgen.writeObject(sourceApplicationTimeSeriesHistogram);
    }


    private void writeAgentHistogram(String fieldName, AgentHistogramList agentHistogramList, JsonGenerator jgen) throws IOException {
        jgen.writeFieldName(fieldName);
        jgen.writeStartObject();
        for (AgentHistogram agentHistogram : agentHistogramList.getCallHistogramList()) {
            jgen.writeFieldName(agentHistogram.getId());
            jgen.writeObject(agentHistogram.getHistogram());
        }
        jgen.writeEndObject();
    }

    private void writeSourceAgentTimeSeriesHistogram(Link link, JsonGenerator jgen) throws IOException {
        AgentResponseTimeViewModelList sourceAgentTimeSeriesHistogram = link.getSourceAgentTimeSeriesHistogram();
        sourceAgentTimeSeriesHistogram.setFieldName("sourceTimeSeriesHistogram");
        jgen.writeObject(sourceAgentTimeSeriesHistogram);
    }

    private void writeSimpleNode(String fieldName, Node node, JsonGenerator jgen) throws IOException {
        jgen.writeFieldName(fieldName);

        jgen.writeStartObject();
        Application application = node.getApplication();
        jgen.writeStringField("applicationName", application.getName());
        jgen.writeStringField("serviceType", application.getServiceType().toString());
        jgen.writeNumberField("serviceTypeCode", application.getServiceTypeCode());
        jgen.writeEndObject();
    }
}
