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

import java.util.Objects;

/**
 * @author jaehong.kim
 */
final class AttributeValueString implements AttributeValue {
    private final String value;

    AttributeValueString(String value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public AttributeValueType getType() {
        return AttributeValueType.STRING;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
