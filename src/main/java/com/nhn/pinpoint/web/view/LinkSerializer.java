package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.web.applicationmap.Link;
import com.nhn.pinpoint.web.applicationmap.Node;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogramList;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.vo.Application;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;
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

        writeSourceHistogram(link, jgen);
        writeTargetHosts(link, jgen);

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

    private void writeTargetHosts(Link link, JsonGenerator jgen) throws IOException {
        CallHistogramList targetList = link.getTargetList();
        Collection<CallHistogram> targetCallHistogramList = targetList.getCallHistogramList();
        jgen.writeFieldName("targetHosts");
        jgen.writeStartObject();
        for (CallHistogram callHistogram : targetCallHistogramList) {
            jgen.writeFieldName(callHistogram.getId());
            jgen.writeStartObject();

            jgen.writeFieldName("histogram");
            jgen.writeObject(callHistogram.getHistogram());

            jgen.writeEndObject();
        }
        jgen.writeEndObject();
    }

    private void writeSourceHistogram(Link link, JsonGenerator jgen) throws IOException {
        jgen.writeFieldName("sourceHistogram");
        final CallHistogramList sourceList = link.getSourceList();
        Collection<CallHistogram> callHistogramList = sourceList.getCallHistogramList();
        jgen.writeStartObject();
        for (CallHistogram callHistogram : callHistogramList) {
            jgen.writeFieldName(callHistogram.getId());
            jgen.writeObject(callHistogram.getHistogram());
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
