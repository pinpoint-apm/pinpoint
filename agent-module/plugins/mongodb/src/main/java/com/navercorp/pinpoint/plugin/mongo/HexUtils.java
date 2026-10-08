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

/**
 * Hex formatting for single bytes such as the BSON binary subtype.
 * <p>
 * {@code String.format("%02X", b)} costs about 270ns and 600 bytes per call, the table lookup costs
 * about 6ns and allocates nothing.
 */
public final class HexUtils {

    private static final String[] HEX = buildHex();

    private HexUtils() {
    }

    private static String[] buildHex() {
        final char[] digits = "0123456789ABCDEF".toCharArray();
        final String[] table = new String[256];
        for (int i = 0; i < table.length; i++) {
            table[i] = new String(new char[]{digits[i >>> 4], digits[i & 0xF]});
        }
        return table;
    }

    /**
     * @return two upper case hex digits of the unsigned value of {@code b}, {@code "80"} for {@code (byte) 0x80}
     */
    public static String toHex(byte b) {
        return HEX[b & 0xFF];
    }
}
