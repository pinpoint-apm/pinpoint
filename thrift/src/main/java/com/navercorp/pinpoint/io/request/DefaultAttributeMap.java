/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.io.request;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class DefaultAttributeMap implements AttributeMap {

    // lazy initialize
    private Map<AttributeKey, Object> attribute;

    protected Map<AttributeKey, Object> getAttributeMap() {
        if (attribute == null) {
            attribute = new HashMap<AttributeKey, Object>();
        }
        return attribute;
    }

    @Override
    public <V> void setAttribute(AttributeKey<V> key, V value) {
        Map<AttributeKey, Object> map = getAttributeMap();
        map.put(key, value);
    }

    @Override
    public <V> V getAttribute(AttributeKey<V> key) {
        return getAttribute(key, key.getDefaultValue());
    }

    @Override
    public <V> V getAttribute(AttributeKey<V> key, V defaultValue) {
        Map<AttributeKey, Object> map = getAttributeMap();
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return (V) value;
        }
    }

}