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

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
@Component
public class TelegrafJsonDeserializer extends ValueDeserializer<TelegrafMetrics> {

    // resolved once by Jackson before this deserializer is first used
    private JavaType metricType;
    private JavaType batchType;

    public TelegrafJsonDeserializer() {
    }

    @Override
    public void resolve(DeserializationContext ctxt) {
        this.metricType = ctxt.constructType(TelegrafMetric.class);
        this.batchType = ctxt.getTypeFactory().constructCollectionType(List.class, metricType);
    }

    @Override
    public TelegrafMetrics deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp.nextToken() != JsonToken.PROPERTY_NAME) {
            ctxt.handleUnexpectedToken(TelegrafMetrics.class, jp);
        }
        String text = jp.getString();
        if ("metrics".equals(text)) {
            // batch
            if (jp.nextToken() != JsonToken.START_ARRAY) {
                ctxt.handleUnexpectedToken(TelegrafMetrics.class, jp);
            }
            List<TelegrafMetric> metrics = jp.readValueAs(batchType);
            // consume the rest of the wrapper object so that the parser ends on its END_OBJECT
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                jp.skipChildren();
            }
            return new TelegrafMetrics(metrics);
        } else {
            // standard
            TelegrafMetric metric = jp.readValueAs(metricType);
            return new TelegrafMetrics(Collections.singletonList(metric));
        }

    }
}
