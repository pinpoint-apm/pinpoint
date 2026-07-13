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

package com.navercorp.pinpoint.common.server.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.mapstruct.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MapStructUtils {

    private final JsonFactory jsonFactory;

    public MapStructUtils(ObjectMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");

        this.jsonFactory = mapper.getFactory();
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface JsonToStringMapList {

    }

    @JsonToStringMapList
    public List<Map<String, String>> jsonToStringMapList(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        return parseList(json, MapStructUtils::readStringMap);
    }

    private static Map<String, String> readStringMap(JsonParser parser, JsonToken token) throws IOException {
        if (token != JsonToken.START_OBJECT) {
            throw new JsonParseException(parser, "expected JSON object");
        }
        final Map<String, String> map = new LinkedHashMap<>();
        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            final String name = parser.currentName();
            final JsonToken valueToken = parser.nextToken();
            if (valueToken == JsonToken.VALUE_NULL) {
                map.put(name, null);
            } else if (valueToken != null && valueToken.isScalarValue()) {
                map.put(name, parser.getText());
            } else {
                throw new JsonParseException(parser, "expected scalar value");
            }
        }
        return map;
    }

    public List<String> jsonToStringList(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        return parseList(json, MapStructUtils::readString);
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface JsonToIntegerList {

    }

    @JsonToIntegerList
    public List<Integer> jsonToIntegerList(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        return parseList(json, MapStructUtils::readInteger);
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface JsonToLongList {

    }

    @JsonToLongList
    public List<Long> jsonToLongList(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        return parseList(json, MapStructUtils::readLong);
    }

    @FunctionalInterface
    private interface ElementReader<T> {
        T read(JsonParser parser, JsonToken token) throws IOException;
    }

    /**
     * Parses a JSON array, mapping {@code null} elements to {@code null} and delegating
     * non-null elements to {@code elementReader}.
     */
    private <T> List<T> parseList(String json, ElementReader<T> elementReader) {
        try (JsonParser parser = jsonFactory.createParser(json)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new JsonParseException(parser, "expected JSON array");
            }
            final List<T> list = new ArrayList<>();
            JsonToken token;
            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token == JsonToken.VALUE_NULL) {
                    list.add(null);
                } else {
                    list.add(elementReader.read(parser, token));
                }
            }
            return list;
        } catch (IOException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
    }

    private static String readString(JsonParser parser, JsonToken token) throws IOException {
        if (token == null || !token.isScalarValue()) {
            throw new JsonParseException(parser, "expected scalar value");
        }
        return parser.getText();
    }

    private static Integer readInteger(JsonParser parser, JsonToken token) throws IOException {
        if (token != JsonToken.VALUE_NUMBER_INT) {
            throw new JsonParseException(parser, "expected integer value");
        }
        return parser.getIntValue();
    }

    private static Long readLong(JsonParser parser, JsonToken token) throws IOException {
        if (token != JsonToken.VALUE_NUMBER_INT) {
            throw new JsonParseException(parser, "expected integer value");
        }
        return parser.getLongValue();
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface ListToJsonStr {

    }

    @ListToJsonStr
    public <T> String listToJsonStr(List<T> lists) {
        if (CollectionUtils.isEmpty(lists)) {
            return "";
        }
        try {
            final StringBuilderWriter buffer = new StringBuilderWriter();
            try (JsonGenerator generator = jsonFactory.createGenerator(buffer)) {
                generator.writeStartArray();
                for (T element : lists) {
                    writeElement(generator, element);
                }
                generator.writeEndArray();
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new JsonRuntimeException("Json Write error", e);
        }
    }

    private void writeElement(JsonGenerator generator, Object element) throws IOException {
        if (element == null) {
            generator.writeNull();
        } else if (element instanceof String string) {
            generator.writeString(string);
        } else if (element instanceof Integer number) {
            generator.writeNumber(number);
        } else if (element instanceof Long number) {
            generator.writeNumber(number);
        } else if (element instanceof Boolean bool) {
            generator.writeBoolean(bool);
        } else {
            // non-scalar element — delegate to the mapper codec bound to the factory
            generator.writeObject(element);
        }
    }
}
