/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.mongo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HexUtilsTest {

    @Test
    void toHex() {
        Assertions.assertEquals("00", HexUtils.toHex((byte) 0x00));
        Assertions.assertEquals("04", HexUtils.toHex((byte) 0x04));
        Assertions.assertEquals("7F", HexUtils.toHex((byte) 0x7F));
        // negative as a byte, user defined BSON subtypes start here
        Assertions.assertEquals("80", HexUtils.toHex((byte) 0x80));
        Assertions.assertEquals("FF", HexUtils.toHex((byte) 0xFF));
    }

    @Test
    void toHex_matchesStringFormat() {
        for (int i = 0; i < 256; i++) {
            byte b = (byte) i;
            Assertions.assertEquals(String.format("%02X", b), HexUtils.toHex(b));
        }
    }
}
