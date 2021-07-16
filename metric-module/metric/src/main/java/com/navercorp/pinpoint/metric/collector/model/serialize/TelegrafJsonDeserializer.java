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

package com.navercorp.pinpoint.metric.collector.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;

import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import com.navercorp.pinpoint.metric.common.model.TagComparator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
@Component
public class TelegrafJsonDeserializer extends JsonDeserializer<TelegrafMetrics> {

    private final static TagComparator TAG_COMPARATOR = new TagComparator();

    public TelegrafJsonDeserializer() {
    }

    @Override
    public TelegrafMetrics deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            ctxt.handleUnexpectedToken(TelegrafMetrics.class, jp);
        }
        String text = jp.getText();
        if ("metrics".equals(text)) {
            // batch
            if (jp.nextToken() != JsonToken.START_ARRAY) {
                ctxt.handleUnexpectedToken(TelegrafMetrics.class, jp);
            }
            TypeReference<List<TelegrafMetric>> batch = new TypeReference<List<TelegrafMetric>>() {
            };
            List<TelegrafMetric> metrics = jp.readValueAs(batch);
            return new TelegrafMetrics(metrics);
        } else {
            // standard
            TelegrafMetric metric = jp.readValueAs(TelegrafMetric.class);
            return new TelegrafMetrics(Collections.singletonList(metric));
        }

    }
}
