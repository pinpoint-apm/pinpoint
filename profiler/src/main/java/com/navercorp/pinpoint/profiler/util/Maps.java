/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import com.google.common.collect.MapMaker;
import com.navercorp.pinpoint.common.util.ConcurrentReferenceHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public final class Maps {

    public static <K, V> ConcurrentMap<K, V> newWeakConcurrentMap() {
        return new ConcurrentReferenceHashMap<K, V>();
    }

    public static <K, V> ConcurrentMap<K, V> newWeakConcurrentMap(int initialCapacity) {
        return new ConcurrentReferenceHashMap<K, V>(initialCapacity);
    }
}
