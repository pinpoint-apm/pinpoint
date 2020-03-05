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

package com.navercorp.pinpoint.common.util.apache;

import java.util.Map;

/**
 * @author emeroad
 */
public final class IntHashMapUtils {

    private IntHashMapUtils() {
    }

    public static <V> IntHashMap<V> copy(Map<Integer, V> target) {
        if (target == null) {
            throw new NullPointerException("target");
        }
        final IntHashMap<V> copyMap = new IntHashMap<V>();
        for (Map.Entry<Integer, V> entry : target.entrySet()) {
            copyMap.put(entry.getKey(), entry.getValue());
        }
        return copyMap;
    }

    public static <V> IntHashMap<V> copyShortMap(Map<Short, V> target) {
        if (target == null) {
            throw new NullPointerException("target");
        }
        final IntHashMap<V> copyMap = new IntHashMap<V>();
        for (Map.Entry<Short, V> entry : target.entrySet()) {
            copyMap.put(entry.getKey(), entry.getValue());
        }
        return copyMap;
    }
}
