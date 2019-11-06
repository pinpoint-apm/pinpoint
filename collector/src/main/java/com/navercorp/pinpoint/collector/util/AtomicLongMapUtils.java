/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.util;

import com.google.common.util.concurrent.AtomicLongMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class AtomicLongMapUtils {

    public static <T> Map<T, Long> remove(AtomicLongMap<T> atomicLongMap) {
        final Map<T, Long> view = atomicLongMap.asMap();

        // view.size() is not recommended, cache entry is striped and volatile field
        final List<T> keySnapshot = keySnapshot(view);

        return remove(atomicLongMap, keySnapshot);
    }

    private static <T> Map<T, Long> remove(AtomicLongMap<T> atomicLongMap, List<T> removeList) {
        final Map<T, Long> remove = new HashMap<>();
        for (T s : removeList) {
            final long value = atomicLongMap.remove(s);
//              check zero ??
//            if (value != 0) {
//                remove.put(s, value);
//            }
            remove.put(s, value);
        }
        return remove;
    }

    private static <T> List<T> keySnapshot(Map<T, Long> view) {
        // Do not use keySet() directly
        return new ArrayList<>(view.keySet());
    }

}
