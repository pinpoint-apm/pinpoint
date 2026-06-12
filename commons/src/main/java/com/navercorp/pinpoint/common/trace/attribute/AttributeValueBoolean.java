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

/**
 * @author jaehong.kim
 */
public final class AttributeValueBoolean implements AttributeValue {
    private static final AttributeValueBoolean TRUE = new AttributeValueBoolean(true);
    private static final AttributeValueBoolean FALSE = new AttributeValueBoolean(false);

    private final boolean value;

    static AttributeValueBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }

    private AttributeValueBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public AttributeValueType getType() {
        return AttributeValueType.BOOLEAN;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public boolean getBooleanValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
