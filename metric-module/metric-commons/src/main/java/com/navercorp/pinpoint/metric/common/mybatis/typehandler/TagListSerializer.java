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
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class TagListSerializer {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectWriter writer;
    private final ObjectReader reader;

    public TagListSerializer(ObjectMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");
        this.writer = mapper.writerFor(Tags.class);
        this.reader = mapper.readerFor(Tags.class);
    }

    public String serialize(List<Tag> tagList) {
        try {
            Tags tags = new Tags(tagList);
            return writer.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing List<Tag> : {}", tagList, e);
            throw new JsonRuntimeException("Error serializing tagList", e);
        }
    }

    public List<Tag> deserialize(String tagListJson) {
        try {
            Tags tags = reader.readValue(tagListJson);
            return tags.getTags();
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing tagList json : {}", tagListJson, e);
            throw new JsonRuntimeException("Error deserializing tagListJson", e);
        }
    }
}
