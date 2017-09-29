/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.util;

import java.util.Collection;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> int nullSafeSize(final Collection<T> collection) {
        return nullSafeSize(collection, 0);
    }

    public static <T> int nullSafeSize(final Collection<T> collection, final int nullValue) {
        if (collection == null) {
            return nullValue;
        }
        return collection.size();
    }

    public static <T> boolean isEmpty(final Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * @deprecated Since 1.7.0. Use {@link CollectionUtils#hasLength(Collection)}
     */
    public static <T> boolean isNotEmpty(final Collection<T> collection) {
        return hasLength(collection);
    }

    public static <T> boolean hasLength(final Collection<T> collection) {
        return collection != null && !collection.isEmpty();
    }


}
