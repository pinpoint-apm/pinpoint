/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.common.model.DoubleCounter;
import com.navercorp.pinpoint.metric.common.model.LongCounter;
import com.navercorp.pinpoint.metric.common.model.MetricType;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.SystemMetricMetadata;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Component
public class SystemMetricJsonDeserializer extends JsonDeserializer<SystemMetric> {
    //    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static long SEC_TO_MILLIS = 1000;

    public SystemMetricJsonDeserializer() {
    }

    @Override
    public SystemMetric deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode jsonNode = jsonParser.readValueAsTree();

        if (!jsonNode.isObject()) {
            return null;
        }

        String metricName = getTextNode(jsonNode, "name");

        JsonNode tagsNode = jsonNode.get("tags");
        if (tagsNode == null || !tagsNode.isObject()) {
            return null;
        }

        String hostName = getTextNode(tagsNode, "host");
        String fieldName = getTextNode(tagsNode, "fieldName");

        List<Tag> tags = deserializeTags(tagsNode);

        long timestamp = jsonNode.get("timestamp").asLong() * SEC_TO_MILLIS;

        JsonNode fieldsNode = jsonNode.get("fields");
        if (fieldsNode == null || !fieldsNode.isObject()) {
            return null;
        }

        String fieldType = getTextNode(fieldsNode, "fieldType");
        JsonNode fieldValueNode = fieldsNode.get("fieldValue");

        if (isInt(fieldType)) {
            long fieldValue = fieldValueNode.asLong();
            return new LongCounter(metricName, hostName, fieldName, fieldValue, tags, timestamp);
        } else {
            double fieldValue = fieldValueNode.asDouble();
            return new DoubleCounter(metricName, hostName, fieldName, fieldValue, tags, timestamp);
        }
    }

    private boolean isInt(String type) {
        if ("int".equals(type)) {
            return true;
        }
        return false;
    }

    private List<Tag> deserializeTags(JsonNode tagsNode) {
        List<Tag> tags = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> tagIterator = tagsNode.fields();
        while (tagIterator.hasNext()) {
            Map.Entry<String, JsonNode> tag = tagIterator.next();
            if (!"host".equals(tag.getKey()) && !"fieldName".equals(tag.getKey())) {
                tags.add(new Tag(tag.getKey(), tag.getValue().asText()));
            }
        }

        return tags;
    }

    private String getTextNode(JsonNode jsonNode, String key) {
        JsonNode childNode = jsonNode.get(key);
        if (childNode == null || !childNode.isTextual()) {
            return null;
        }

        String value = childNode.asText();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }
}
