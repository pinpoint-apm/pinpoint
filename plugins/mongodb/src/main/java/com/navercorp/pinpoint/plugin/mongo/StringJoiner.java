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

package com.navercorp.pinpoint.plugin.mongo;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class StringJoiner {

    private StringJoiner() {
    }

    public static String join(final Collection<String> collection, final String separator) {
        if (collection == null) {
            return null;
        }
        final int size = collection.size();
        if (size == 0) {
            return "";
        }
        if (size == 1) {
            final Iterator<String> iterator = collection.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                throw new ConcurrentModificationException("size:" + collection.size());
            }
        }

        final int bufferSize = getBufferSize(collection, separator);

        final StringBuilder builder = new StringBuilder(bufferSize);
        final Iterator<String> iterator = collection.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (!iterator.hasNext()) {
                break;
            }
            builder.append(separator);
        }

        return builder.toString();
    }

    private static int getBufferSize(Collection<String> collection, String separator) {
        int bufferSize = 0;

        final Iterator<String> iterator = collection.iterator();
        while (iterator.hasNext()) {
            final String value = iterator.next();
            // null == "null"
            bufferSize += StringUtils.getLength(value, 4);
            if (!iterator.hasNext()) {
                break;
            }
            bufferSize += separator.length();
        }
        return bufferSize;
    }
}
