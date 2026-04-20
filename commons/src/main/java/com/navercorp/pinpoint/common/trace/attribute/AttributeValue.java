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

package com.navercorp.pinpoint.common.trace.attribute;

import java.util.List;
import java.util.Map;

/**
 * Typed attribute value for Span/SpanEvent attributes.
 * Inspired by OpenTelemetry's {@code io.opentelemetry.api.common.Value<T>}.
 *
 * @author jaehong.kim
 */
public interface AttributeValue {

    AttributeValueType getType();

    Object getValue();

    static AttributeValue of(String value) {
        return new AttributeValueString(value);
    }

    static AttributeValue of(boolean value) {
        return new AttributeValueBoolean(value);
    }

    static AttributeValue of(long value) {
        return new AttributeValueLong(value);
    }

    static AttributeValue of(double value) {
        return new AttributeValueDouble(value);
    }

    static AttributeValue of(byte[] value) {
        return new AttributeValueBytes(value);
    }

    static AttributeValue of(AttributeValue... values) {
        return new AttributeValueArray(values);
    }

    static AttributeValue of(List<AttributeValue> values) {
        return new AttributeValueArray(values);
    }

    static AttributeValue ofAttributeKeyValueList(AttributeKeyValue... values) {
        return new AttributeKeyValueList(values);
    }

    static AttributeValue ofAttributeKeyValueList(Map<String, AttributeValue> values) {
        return new AttributeKeyValueList(values);
    }
}
