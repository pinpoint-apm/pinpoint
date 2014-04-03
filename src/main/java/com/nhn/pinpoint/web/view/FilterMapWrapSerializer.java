package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nhn.pinpoint.web.applicationmap.FilterMapWrap;
import com.nhn.pinpoint.web.applicationmap.Node;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.ApplicationScatterScanResult;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public class FilterMapWrapSerializer extends JsonSerializer<FilterMapWrap> {
    @Override
    public void serialize(FilterMapWrap wrap, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeObjectField("applicationMapData", wrap.getApplicationMap());

        jgen.writeNumberField("lastFetchedTimestamp", wrap.getLastFetchedTimestamp());

        final List<ApplicationScatterScanResult> applicationScatterScanResult = wrap.getApplicationScatterScanResult();

        jgen.writeFieldName("applicationScatterScanResult");
        jgen.writeStartObject();
        for (ApplicationScatterScanResult scatterScanResult : applicationScatterScanResult) {
            Application application = scatterScanResult.getApplication();
            String name = application.getName() + Node.NODE_DELIMITER + application.getServiceType().toString();
            jgen.writeObjectField(name, scatterScanResult.getScatterScanResult());
        }
        jgen.writeEndObject();


        jgen.writeEndObject();
    }
}
