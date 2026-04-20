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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
final class AttributeValueArray implements AttributeValue {
    private final List<AttributeValue> value;

    AttributeValueArray(AttributeValue... values) {
        Objects.requireNonNull(values, "values");
        this.value = Collections.unmodifiableList(Arrays.asList(values));
    }

    AttributeValueArray(List<AttributeValue> values) {
        Objects.requireNonNull(values, "values");
        this.value = Collections.unmodifiableList(values);
    }

    @Override
    public AttributeValueType getType() {
        return AttributeValueType.ARRAY;
    }

    @Override
    public List<AttributeValue> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
