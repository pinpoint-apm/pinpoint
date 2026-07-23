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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.util.Utf8;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Component
public class OtlpAttributeBoMapper {

    private final int attributeValueMaxBytes;

    public OtlpAttributeBoMapper(@Value("${pinpoint.collector.otlptrace.attribute.value-max-bytes:8192}") int attributeValueMaxBytes) {
        if (attributeValueMaxBytes < 0) {
            throw new IllegalArgumentException("attributeValueMaxBytes must be >= 0: " + attributeValueMaxBytes);
        }
        this.attributeValueMaxBytes = attributeValueMaxBytes;
    }

    /**
     * Converts attributes to an {@link AttributeBo} list, applying {@code excludeFilter}, the fixed
     * security policy ({@link OtlpSensitiveAttributeFilter} — drops sensitive keys and strips
     * query/fragment/userinfo from URL values) and truncating over-long string (UTF-8 byte length)
     * and byte (raw byte length) values in the same pass, recursing into ARRAY / KVLIST. Numeric
     * and boolean values are left untouched, matching the OTel spec (only string and byte values
     * are length-limited). {@code onTruncated} is invoked once per truncated leaf so the caller can
     * emit a single per-span {@code OPENTELEMETRY_TRUNCATED} summary.
     */
    public List<AttributeBo> toAttributeBoList(Map<String, AttributeValue> attributes, Predicate<String> excludeFilter, TruncationListener onTruncated) {
        Objects.requireNonNull(onTruncated, "onTruncated");
        if (attributes.isEmpty()) {
            return List.of();
        }
        List<AttributeBo> result = new ArrayList<>();
        for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            // Caller exclusion (promoted / statically-filtered keys) + fixed security policy.
            if (excludeFilter.test(key) || OtlpSensitiveAttributeFilter.isSensitive(key)) {
                continue;
            }
            // Strip query/fragment/userinfo from URL values before truncation.
            final AttributeValue sanitized = sanitizeValue(key, entry.getValue());
            final AttributeValue value = truncateValue(sanitized, onTruncated);
            result.add(new AttributeBo(key, value));
        }
        return result;
    }

    private AttributeValue sanitizeValue(String key, AttributeValue value) {
        if (value.getType() != AttributeValueType.STRING) {
            return value;
        }
        final String original = (String) value.getValue();
        final String sanitized = OtlpSensitiveAttributeFilter.sanitizeUrl(key, original);
        if (sanitized == null || sanitized.equals(original)) {
            return value;
        }
        return AttributeValue.of(sanitized);
    }

    @SuppressWarnings("unchecked")
    private AttributeValue truncateValue(AttributeValue value, TruncationListener onTruncated) {
        switch (value.getType()) {
            case STRING: {
                final String truncated = Utf8.truncate((String) value.getValue(), attributeValueMaxBytes);
                if (truncated == null) {
                    return value;
                }
                onTruncated.truncated();
                return AttributeValue.of(truncated);
            }
            case BYTES: {
                final byte[] bytes = (byte[]) value.getValue();
                if (bytes.length <= attributeValueMaxBytes) {
                    return value;
                }
                onTruncated.truncated();
                return AttributeValue.of(Arrays.copyOf(bytes, attributeValueMaxBytes));
            }
            case ARRAY: {
                final List<AttributeValue> array = (List<AttributeValue>) value.getValue();
                boolean changed = false;
                final List<AttributeValue> result = new ArrayList<>(array.size());
                for (AttributeValue item : array) {
                    final AttributeValue newItem = truncateValue(item, onTruncated);
                    result.add(newItem);
                    changed |= (newItem != item);
                }
                return changed ? AttributeValue.of(result) : value;
            }
            case KEY_VALUE_LIST: {
                final List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) value.getValue();
                boolean changed = false;
                final AttributeKeyValue[] result = new AttributeKeyValue[kvList.size()];
                for (int i = 0; i < kvList.size(); i++) {
                    final AttributeKeyValue entry = kvList.get(i);
                    final AttributeValue newValue = truncateValue(entry.getValue(), onTruncated);
                    final boolean entryChanged = newValue != entry.getValue();
                    result[i] = entryChanged ? AttributeKeyValue.of(entry.getKey(), newValue) : entry;
                    changed |= entryChanged;
                }
                return changed ? AttributeValue.ofAttributeKeyValueList(result) : value;
            }
            default:
                // BOOLEAN / LONG / DOUBLE — never truncated (per OTel spec)
                return value;
        }
    }
}
