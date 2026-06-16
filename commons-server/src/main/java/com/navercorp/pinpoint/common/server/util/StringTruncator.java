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

package com.navercorp.pinpoint.common.server.util;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class StringTruncator {

    private StringTruncator() {
    }

    /**
     * Truncates a string to at most {@code maxBytes} UTF-8 bytes without splitting a multi-byte
     * character.
     *
     * @param str      the string to truncate; must not be {@code null}
     * @param maxBytes the maximum length in UTF-8 bytes
     * @return the truncated string, or {@code null} when no truncation is needed
     * (value already within the limit)
     */
    public static @Nullable String truncateUtf8(String str, int maxBytes) {
        Objects.requireNonNull(str, "str");
        final int len = str.length();
        // fast path: a UTF-8 char is at most 4 bytes, so when 4*len <= maxBytes
        // the value cannot exceed the limit -> no truncation.
        if ((long) len * 4 <= maxBytes) {
            return null;
        }
        int byteCount = 0;
        for (int i = 0; i < len; ) {
            final int cp = str.codePointAt(i);
            final int bytes;
            if (cp <= 0x7F) {
                bytes = 1;
            } else if (cp <= 0x7FF) {
                bytes = 2;
            } else if (cp <= 0xFFFF) {
                bytes = 3;
            } else {
                bytes = 4;
            }
            if (byteCount + bytes > maxBytes) {
                return str.substring(0, i); // cut at the code-point boundary
            }
            byteCount += bytes;
            i += Character.charCount(cp); // 1 or 2 (surrogate pair)
        }
        return null; // within limit
    }
}