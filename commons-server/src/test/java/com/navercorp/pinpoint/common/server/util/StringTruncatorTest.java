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

import com.google.common.base.Utf8;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringTruncatorTest {

    private static int utf8Len(String value) {
        return Utf8.encodedLength(value); // Guava's Utf8 counts code points, not bytes
    }

    @Test
    void truncateUtf8_withinLimit_returnsNull() {
        assertThat(StringTruncator.truncateUtf8("short", 64)).isNull();
    }

    @Test
    void truncateUtf8_exactlyAtLimit_returnsNull() {
        // bytes.length == maxBytes => no truncation
        String value = "abcde";
        assertThat(StringTruncator.truncateUtf8(value, utf8Len(value))).isNull();
    }

    @Test
    void truncateUtf8_emptyString_returnsNull() {
        assertThat(StringTruncator.truncateUtf8("", 8)).isNull();
    }

    @Test
    void truncateUtf8_fastPathBoundary_returnsNull() {
        // 4 * len(3) == maxBytes(12) triggers the upper-bound fast path
        assertThat(StringTruncator.truncateUtf8("abc", 12)).isNull();
    }

    @Test
    void truncateUtf8_nullValue_throwsNpe() {
        assertThatThrownBy(() -> StringTruncator.truncateUtf8(null, 8))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void truncateUtf8_asciiOverLimit_truncatedToMaxBytes() {
        String result = StringTruncator.truncateUtf8("a".repeat(100), 10);

        assertThat(result).isEqualTo("a".repeat(10));
        assertThat(utf8Len(result)).isEqualTo(10);
    }

    @Test
    void truncateUtf8_multiByteBoundary_doesNotSplitCharacter() {
        // '가' is 3 UTF-8 bytes (0xEA 0xB0 0x80). "가가가" = 9 bytes.
        // maxBytes=4 lands inside the 2nd char => must back off to the char boundary (3 bytes).
        String result = StringTruncator.truncateUtf8("가가가", 4);

        assertThat(result).isEqualTo("가");
        assertThat(utf8Len(result)).isLessThanOrEqualTo(4);
    }

    @Test
    void truncateUtf8_multiByteExactBoundary_keepsWholeCharacters() {
        // maxBytes=6 == exactly two '가' chars; no back-off needed.
        String result = StringTruncator.truncateUtf8("가가가", 6);

        assertThat(result).isEqualTo("가가");
        assertThat(utf8Len(result)).isEqualTo(6);
    }
}