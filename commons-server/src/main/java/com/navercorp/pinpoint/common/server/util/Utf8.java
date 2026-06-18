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

public final class Utf8 {

    private Utf8() {
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
    public static @Nullable String truncate(String str, int maxBytes) {
        final int index = truncateIndex(str, maxBytes);
        if (index == str.length()) {
            return null; // within limit
        }
        return str.substring(0, index);
    }

    /**
     * Returns the char index marking the end of the longest prefix of {@code str} whose UTF-8 byte
     * length is within {@code maxBytes}, never splitting a multi-byte character. Equals
     * {@code str.length()} when the whole string already fits.
     *
     * @param str      the string to measure; must not be {@code null}
     * @param maxBytes the maximum length in UTF-8 bytes
     * @return the cut index in {@code [0, str.length()]}
     */
    public static int truncateIndex(String str, int maxBytes) {
        Objects.requireNonNull(str, "str");
        final int utf16Length = str.length();
        // fast path: a UTF-8 char is at most 4 bytes, so when 4*utf16Length <= maxBytes
        // the value cannot exceed the limit -> no truncation.
        if ((long) utf16Length * 4 <= maxBytes) {
            return utf16Length;
        }
        int utf8Length = 0;
        for (int i = 0; i < utf16Length; ) {
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
            if (utf8Length + bytes > maxBytes) {
                return i; // cut at the code-point boundary
            }
            utf8Length += bytes;
            i += Character.charCount(cp); // 1 or 2 (surrogate pair)
        }
        return utf16Length; // within limit
    }
}