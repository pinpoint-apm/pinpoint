/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.mybatis.typehandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class TagListSerializer {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JsonFactory jsonFactory;

    public TagListSerializer(ObjectMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");
        this.jsonFactory = mapper.getFactory();
    }

    public String serialize(List<Tag> tagList) {
        try {
            final StringBuilderWriter buffer = new StringBuilderWriter();
            try (JsonGenerator generator = jsonFactory.createGenerator(buffer)) {
                generator.writeStartObject();
                for (Tag tag : tagList) {
                    generator.writeStringField(tag.getName(), tag.getValue());
                }
                generator.writeEndObject();
            }
            return buffer.toString();
        } catch (IOException e) {
            logger.error("Error serializing List<Tag> : {}", tagList, e);
            throw new JsonRuntimeException("Error serializing tagList", e);
        }
    }

    public List<Tag> deserialize(String tagListJson) {
        try (JsonParser parser = jsonFactory.createParser(tagListJson)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new JsonParseException(parser, "expected JSON object");
            }
            final List<Tag> tagList = new ArrayList<>();
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                final String name = parser.currentName();
                final JsonToken valueToken = parser.nextToken();
                // VALUE_NULL is also rejected — Tag does not allow a null value
                if (valueToken == null || valueToken == JsonToken.VALUE_NULL || !valueToken.isScalarValue()) {
                    throw new JsonParseException(parser, "expected non-null scalar tag value");
                }
                tagList.add(new Tag(name, parser.getValueAsString()));
            }
            return tagList;
        } catch (IOException e) {
            logger.error("Error deserializing tagList json : {}", tagListJson, e);
            throw new JsonRuntimeException("Error deserializing tagListJson", e);
        }
    }
}
