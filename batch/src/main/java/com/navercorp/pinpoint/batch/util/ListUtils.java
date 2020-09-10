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

package com.navercorp.pinpoint.batch.util;


import java.util.List;

/**
 * @author Taejin Koo
 */
public final class ListUtils {

    private ListUtils() {
    }

    public static <V> V getFirst(List<V> list) {
        return getFirst(list, null);
    }

    public static <V> V getFirst(List<V> list, V defaultValue) {
        if (list == null) {
            return defaultValue;
        }

        int size = list.size();
        if (size > 0) {
            return list.get(0);
        } else {
            return defaultValue;
        }
    }

}
