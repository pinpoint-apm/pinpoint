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
import com.navercorp.pinpoint.metric.common.model.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Hyunjoon Cho
 */
@Component
public class TelegrafHttpJsonDeserializer extends JsonDeserializer<Metrics> {


    private final static TagComparator TAG_COMPARATOR = new TagComparator();

    private final String[] ignoreTagNames = {"host"};

    public TelegrafHttpJsonDeserializer() {
    }

    @Override
    public Metrics deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode root = jsonParser.readValueAsTree();

        List<SystemMetric> metricList = new ArrayList<>();

        for (JsonNode jsonNode : root.get("metrics")) {
            List<SystemMetric> systemMetrics = getSystemMetrics(jsonNode);
            metricList.addAll(systemMetrics);
        }

        return new Metrics(metricList);
    }

    private List<SystemMetric> getSystemMetrics(JsonNode jsonNode) {
        if (!jsonNode.isObject()) {
            return Collections.emptyList();
        }

        String metricName = getTextNode(jsonNode, "name");

        JsonNode tagsNode = jsonNode.get("tags");
        if (tagsNode == null || !tagsNode.isObject()) {
            return Collections.emptyList();
        }

        String hostName = getTextNode(tagsNode, "host");

        List<Tag> tags = deserializeTags(tagsNode);

        long timestamp = TimeUnit.SECONDS.toMillis(jsonNode.get("timestamp").asLong());

        JsonNode fieldsNode = jsonNode.get("fields");
        if (fieldsNode == null || !fieldsNode.isObject()) {
            return Collections.emptyList();
        }
        return getSystemMetrics(fieldsNode, metricName, hostName, tags, timestamp);
    }


    private List<SystemMetric> getSystemMetrics(JsonNode fieldsNode, String metricName, String hostName, List<Tag> tags, long timestamp) {
        List<SystemMetric> metricList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = fieldsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> fieldNode = fields.next();
            SystemMetric systemMetric = readMetric(metricName, fieldNode, hostName, timestamp, tags);
            metricList.add(systemMetric);
        }
        return metricList;
    }

    private SystemMetric readMetric(String metricName, Map.Entry<String, JsonNode> fieldNode, String hostName, long timestamp, List<Tag> tags) {
        String fieldName = fieldNode.getKey();
        double fieldValue = fieldNode.getValue().asDouble();
        return new DoubleCounter(metricName, hostName, fieldName, fieldValue, tags, timestamp);
    }

    private List<Tag> deserializeTags(JsonNode tagsNode) {
        List<Tag> tags = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> tagIterator = tagsNode.fields();
        while (tagIterator.hasNext()) {
            Map.Entry<String, JsonNode> tag = tagIterator.next();
            if (isIgnoreHeaderNames(tag.getKey())) {
                continue;
            }
            tags.add(new Tag(tag.getKey(), tag.getValue().asText()));
        }

//        tags.sort(TAG_COMPARATOR);
        return tags;
    }

    private boolean isIgnoreHeaderNames(String tagName) {
        for (String ignoreTagName : ignoreTagNames) {
            if (ignoreTagName.equals(tagName)) {
                return true;
            }
        }
        return false;
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
