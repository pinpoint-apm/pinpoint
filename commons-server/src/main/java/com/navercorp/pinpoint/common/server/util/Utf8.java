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
        // Per-char scan (cf. com.google.common.base.Utf8#encodedLength): branch on the UTF-16 unit
        // and short-circuit ASCII first, avoiding codePointAt's surrogate combine + Character.charCount.
        int utf8Length = 0;
        int i = 0;
        while (i < utf16Length) {
            final char c = str.charAt(i);
            final int bytes;
            final int step;
            if (c < 0x80) {
                bytes = 1;
                step = 1;
            } else if (c < 0x800) {
                bytes = 2;
                step = 1;
            } else if (Character.isHighSurrogate(c)
                    && i + 1 < utf16Length && Character.isLowSurrogate(str.charAt(i + 1))) {
                bytes = 4; // surrogate pair -> one code point, 4 UTF-8 bytes across 2 chars
                step = 2;
            } else {
                bytes = 3; // BMP char (or an unpaired surrogate, kept lenient as 3 bytes)
                step = 1;
            }
            if (utf8Length + bytes > maxBytes) {
                return i; // cut at the char/code-point boundary
            }
            utf8Length += bytes;
            i += step;
        }
        return utf16Length; // within limit
    }
}