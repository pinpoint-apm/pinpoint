package com.navercorp.pinpoint.metric.common.model.mybatis;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.io.IOException;
import java.util.List;

public class TagMySqlSerializer extends JsonSerializer<List<Tag>> {

    @Override
    public void serialize(List<Tag> tags, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Tag tag : tags) {
            gen.writeStringField(tag.getName(), tag.getValue());
        }
        gen.writeEndObject();
    }

}
