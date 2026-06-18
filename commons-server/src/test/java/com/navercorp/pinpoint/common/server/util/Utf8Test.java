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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Utf8Test {

    private static int utf8Len(String value) {
        return com.google.common.base.Utf8.encodedLength(value); // Guava's Utf8 counts code points, not bytes
    }

    @Test
    void truncate_withinLimit_returnsNull() {
        assertThat(Utf8.truncate("short", 64)).isNull();
    }

    @Test
    void truncate_exactlyAtLimit_returnsNull() {
        // bytes.length == maxBytes => no truncation
        String value = "abcde";
        assertThat(Utf8.truncate(value, utf8Len(value))).isNull();
    }

    @Test
    void truncate_emptyString_returnsNull() {
        assertThat(Utf8.truncate("", 8)).isNull();
    }

    @Test
    void truncate_fastPathBoundary_returnsNull() {
        // 4 * len(3) == maxBytes(12) triggers the upper-bound fast path
        assertThat(Utf8.truncate("abc", 12)).isNull();
    }

    @Test
    void truncate_nullValue_throwsNpe() {
        assertThatThrownBy(() -> Utf8.truncate(null, 8))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void truncate_asciiOverLimit_truncatedToMaxBytes() {
        String result = Utf8.truncate("a".repeat(100), 10);

        assertThat(result).isEqualTo("a".repeat(10));
        assertThat(utf8Len(result)).isEqualTo(10);
    }

    @Test
    void truncate_multiByteBoundary_doesNotSplitCharacter() {
        // '가' is 3 UTF-8 bytes (0xEA 0xB0 0x80). "가가가" = 9 bytes.
        // maxBytes=4 lands inside the 2nd char => must back off to the char boundary (3 bytes).
        String result = Utf8.truncate("가가가", 4);

        assertThat(result).isEqualTo("가");
        assertThat(utf8Len(result)).isLessThanOrEqualTo(4);
    }

    @Test
    void truncate_multiByteExactBoundary_keepsWholeCharacters() {
        // maxBytes=6 == exactly two '가' chars; no back-off needed.
        String result = Utf8.truncate("가가가", 6);

        assertThat(result).isEqualTo("가가");
        assertThat(utf8Len(result)).isEqualTo(6);
    }

    // U+1F600 (😀) is a surrogate pair (😀) encoding to 4 UTF-8 bytes.
    private static final String EMOJI = "😀";

    @Test
    void truncate_surrogatePair_keptWhenWithinLimit() {
        assertThat(Utf8.truncate(EMOJI, 4)).isNull(); // 4 bytes fits exactly
    }

    @Test
    void truncate_surrogatePair_droppedWholeNotSplit() {
        // maxBytes=3 cannot hold the 4-byte pair, so it is dropped whole (never a lone surrogate)
        assertThat(Utf8.truncate(EMOJI, 3)).isEmpty();
    }

    @Test
    void truncate_surrogatePairAfterAscii_cutsBeforePair() {
        // 'a'(1) + pair(4) = 5 bytes; maxBytes=3 keeps 'a' and drops the un-splittable pair
        assertThat(Utf8.truncate("a" + EMOJI, 3)).isEqualTo("a");
    }
}