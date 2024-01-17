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
import java.util.Objects;

@Component
public class MapStructUtils {
    private final ObjectMapper mapper;

    public MapStructUtils(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }


    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface JsonStrToList {

    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface listToJsonStr {

    }


    @JsonStrToList
    public <T> List<T> jsonStrToList(String s) {
        if (StringUtils.isEmpty(s)) {
            return Collections.emptyList();
        }
        try {
            List<T> value = mapper.readValue(s, new TypeReference<List<T>>() {});
            return value;
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
    }

    public List<Long> jsonStrToLongList(String s) {
        if (StringUtils.isEmpty(s)) {
            return Collections.emptyList();
        }
        try {
            return mapper.readValue(s, new TypeReference<List<Long>>() {});
        } catch (JacksonException e) {
            throw new JsonRuntimeException("Json read error", e);
        }
    }

    @listToJsonStr
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
