package com.navercorp.pinpoint.metric.common.model;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class TagSerializer extends JsonSerializer<List<Tag>> {
    @Override
    public void serialize(List<Tag> tags, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        for (Tag tag : tags) {
            gen.writeString(tag.toString());
        }
        gen.writeEndArray();
    }
}
