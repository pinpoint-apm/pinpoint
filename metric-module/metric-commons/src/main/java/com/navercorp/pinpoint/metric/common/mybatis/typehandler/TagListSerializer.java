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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author minwoo-jung
 */
public class TagListSerializer {

    private final static Logger logger = LogManager.getLogger(TagListSerializer.class.getName());

    private final static ObjectMapper OBJECT_MAPPER = getMapper();

    static ObjectMapper getMapper() {
        return Jackson.newBuilder()
                .serializerByType(List.class, new TagMySqlSerializer())
                .build();
    }

    public static String serialize(List<Tag> tagList) {
        try {
            return OBJECT_MAPPER.writeValueAsString(tagList);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing List<Tag> : {}", tagList, e);
            throw new JsonRuntimeException("Error serializing tagList", e);
        }
    }

    public static List<Tag> deserialize(String tagListJson) {
        try {
            Tags tags = OBJECT_MAPPER.readValue(tagListJson, Tags.class);
            return tags.getTags();
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing tagList json : {}", tagListJson, e);
            throw new JsonRuntimeException("Error deserializing tagListJson", e);
        }
    }
}
