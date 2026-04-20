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
public final class AttributeKeyValue {

    private final String key;
    private final AttributeValue value;

    private AttributeKeyValue(String key, AttributeValue value) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
    }

    public static AttributeKeyValue of(String key, AttributeValue value) {
        return new AttributeKeyValue(key, value);
    }

    public String getKey() {
        return key;
    }

    public AttributeValue getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
