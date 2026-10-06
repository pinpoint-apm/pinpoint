/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.mapper;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts OTLP attribute values to the strings Pinpoint stores as tags.
 * The same rule applies to resource and data point attributes.
 */
public final class AttributeValues {

    static final String UNSUPPORTED_VALUE = "Unsupported value type.";

    private AttributeValues() {
    }

    public static String toString(AnyValue value) {
        if (value.hasStringValue()) {
            return value.getStringValue();
        } else if (value.hasIntValue()) {
            return String.valueOf(value.getIntValue());
        } else if (value.hasDoubleValue()) {
            return String.valueOf(value.getDoubleValue());
        } else if (value.hasBoolValue()) {
            return String.valueOf(value.getBoolValue());
        } else if (value.hasKvlistValue()) {
            return String.valueOf(value.getKvlistValue());
        } else {
            return UNSUPPORTED_VALUE;
        }
    }

    public static Map<String, String> toMap(List<KeyValue> attributes) {
        Map<String, String> tags = new HashMap<>();
        for (KeyValue attribute : attributes) {
            tags.put(attribute.getKey(), toString(attribute.getValue()));
        }
        return tags;
    }
}
