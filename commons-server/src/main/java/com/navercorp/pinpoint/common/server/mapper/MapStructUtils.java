/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.server.mapper;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.mapstruct.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MapStructUtils {

    private final ObjectMapper mapper;

    private final ObjectReader integerListReader;
    private final ObjectReader longListReader;
    private final ObjectReader stringListReader;
    private final ObjectReader stringMapListReader;

    public MapStructUtils(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");

        this.integerListReader = mapper.readerForListOf(Integer.class);
        this.longListReader = mapper.readerForListOf(Long.class);
        this.stringListReader = mapper.readerForListOf(String.class);
        this.stringMapListReader = mapper.readerFor(new TypeReference<List<Map<String, String>>>() {});
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
        try {
            return stringMapListReader.readValue(json);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
    }

    public List<String> jsonToStringList(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        try {
            return stringListReader.readValue(json);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
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
        try {
            return integerListReader.readValue(json);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
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
        try {
            return longListReader.readValue(json);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
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
            return mapper.writeValueAsString(lists);
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json Write error", e);
        }
    }
}
