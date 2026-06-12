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
 * <p>
 * {@link #getValue()} returns the wrapped value as {@link Object} (boxing primitives).
 * On hot paths, prefer the per-type accessors on the concrete classes
 * (e.g. {@link AttributeValueLong#getLongValue()}) to avoid boxing.
 *
 * @author jaehong.kim
 */
public interface AttributeValue {

    AttributeValueType getType();

    Object getValue();

    static AttributeValueString of(String value) {
        return new AttributeValueString(value);
    }

    static AttributeValueBoolean of(boolean value) {
        return AttributeValueBoolean.of(value);
    }

    static AttributeValueLong of(long value) {
        return new AttributeValueLong(value);
    }

    static AttributeValueDouble of(double value) {
        return new AttributeValueDouble(value);
    }

    static AttributeValueBytes of(byte[] value) {
        return new AttributeValueBytes(value);
    }

    static AttributeValueArray of(AttributeValue... values) {
        return new AttributeValueArray(values);
    }

    static AttributeValueArray of(List<AttributeValue> values) {
        return new AttributeValueArray(values);
    }

    static AttributeKeyValueList ofAttributeKeyValueList(AttributeKeyValue... values) {
        return new AttributeKeyValueList(values);
    }

    static AttributeKeyValueList ofAttributeKeyValueList(Map<String, AttributeValue> values) {
        return new AttributeKeyValueList(values);
    }
}