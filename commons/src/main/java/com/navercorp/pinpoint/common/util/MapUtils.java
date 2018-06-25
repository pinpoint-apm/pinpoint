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

package com.navercorp.pinpoint.common.util;

import java.util.Map;

/**
 * @since 1.7.2
 * @author Woonduk Kang(emeroad)
 */
public final class MapUtils {

    private MapUtils() {
    }

    public static <T> boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }


    public static boolean hasLength(final Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static <T> int nullSafeSize(final Map<?, ?> map) {
        return nullSafeSize(map, 0);
    }

    public static <T> int nullSafeSize(final Map<?, ?> map, final int nullValue) {
        if (map == null) {
            return nullValue;
        }
        return map.size();
    }
}
