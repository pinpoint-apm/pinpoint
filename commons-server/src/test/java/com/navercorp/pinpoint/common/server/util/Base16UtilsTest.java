/*
 * Copyright 2026 NAVER Corp.
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

class Base16UtilsTest {

    private static final byte[] BYTES = {(byte) 0xAB, 0x01, (byte) 0xFF};

    @Test
    void encode_lowerCase() {
        assertThat(Base16Utils.encodeToString(BYTES)).isEqualTo("ab01ff");
        assertThat(Base16Utils.encodeToString(new byte[0])).isEmpty();
    }

    @Test
    void decode_eitherCase() {
        assertThat(Base16Utils.decodeToBytes("ab01ff")).isEqualTo(BYTES);
        assertThat(Base16Utils.decodeToBytes("AB01FF")).isEqualTo(BYTES);
        assertThat(Base16Utils.decodeToBytes("")).isEmpty();
    }

    @Test
    void decode_rejectsOddLengthAndNonHex() {
        assertThatThrownBy(() -> Base16Utils.decodeToBytes("ab0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base16Utils.decodeToBytes("zz")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base16Utils.decodeToBytes("ab 1")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void encodedLength() {
        assertThat(Base16Utils.encodedLength(0)).isZero();
        assertThat(Base16Utils.encodedLength(16)).isEqualTo(32);
        assertThat(Base16Utils.encodedLength(Integer.MAX_VALUE)).isEqualTo(2L * Integer.MAX_VALUE);
    }
}
