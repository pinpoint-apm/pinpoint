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

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.Charsets;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author HyunGil Jeong
 */
public class AgentUuidUtils {

    private static final char[] CODE_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '-', '_'
    };

    private static final InverseLookup INVERSE_LOOKUP = new InverseLookup(CODE_TABLE);

    private static class InverseLookup {
        private final int[] inverseCodeTable = new int[256];

        private InverseLookup(char[] codeTable) {
            Arrays.fill(inverseCodeTable, -1);
            for (int i = 0; i < codeTable.length; i++) {
                int idx = codeTable[i] & BIT_MASK_8;
                inverseCodeTable[idx] = i;
            }
        }

        private int lookup(byte c) {
            int idx = inverseCodeTable[c & BIT_MASK_8];
            if (idx < 0) {
                throw new IllegalArgumentException("Invalid char found: " + c);
            }
            return idx;
        }
    }

    private static final int BIT_MASK_6 = 0x3f;
    private static final int BIT_MASK_8 = 0xff;

    /**
     * Base64 encodes the given {@code uuidString} into URL and filename safe string as specified by RFC 4648 section 5.
     * The returned string is guaranteed to be 22 characters long (with 2 pad characters "=" removed) as a valid UUID
     * string will always be encoded into the same length.
     *
     * @param uuidString uuid string to encode
     * @return URL and filename safe base64 encoded string with pad characters removed
     *
     * @throws NullPointerException if {@code uuidString} is null
     * @throws IllegalArgumentException if {@code uuidString} is not a valid string representation of uuid
     */
    public static String encode(String uuidString) {
        if (uuidString == null) {
            throw new NullPointerException("uuidString");
        }
        UUID uuid = UUID.fromString(uuidString);
        return encode(uuid);
    }

    /**
     * Base64 encodes the given {@code uuid} into URL and filename safe string as specified by RFC 4648 section 5.
     * The returned string is guaranteed to be 22 characters long (with 2 pad characters "=" removed) as a valid UUID
     * string will always be encoded into the same length.
     *
     * @param uuid uuid to encode
     * @return URL and filename safe base64 encoded string with pad characters removed
     *
     * @throws NullPointerException if {@code uuid} is null
     * @throws IllegalArgumentException if {@code uuid} is not a valid uuid
     */
    public static String encode(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("uuid");
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] encoded = encodeUuidBytes(byteBuffer.array());
        return new String(encoded, Charsets.US_ASCII);
    }

    private static byte[] encodeUuidBytes(byte[] src) {
        if (src.length != 16) {
            throw new IllegalArgumentException("Invalid src byte array: " + BytesUtils.toString(src));
        }
        int srcPtr;
        int dstPtr;
        byte[] dst = new byte[22];
        // first 15 bytes, in groups of 3
        for (int i = 0; i < 5; i++) {
            //               1         2
            //     012345678901234567890123
            // src |......||......||......| -> [11111111][11111111][11111111]
            // dst |....||....||....||....| -> [00111111][00111111][00111111][00111111]
            srcPtr = i * 3;
            dstPtr = i * 4;
            int currentBits;
            currentBits  = (src[srcPtr  ] & BIT_MASK_8) << 16;
            currentBits |= (src[srcPtr+1] & BIT_MASK_8) << 8;
            currentBits |= (src[srcPtr+2] & BIT_MASK_8);
            dst[dstPtr  ] = (byte) CODE_TABLE[(currentBits >>> 18) & BIT_MASK_6];
            dst[dstPtr+1] = (byte) CODE_TABLE[(currentBits >>> 12) & BIT_MASK_6];
            dst[dstPtr+2] = (byte) CODE_TABLE[(currentBits >>>  6) & BIT_MASK_6];
            dst[dstPtr+3] = (byte) CODE_TABLE[ currentBits         & BIT_MASK_6];
        }
        // left over last byte
        //     01234567
        // src |......| -> [11111111]
        // dst |....||| -> [00111111][00110000]
        int currentBits = src[15] & BIT_MASK_8;
        dst[20] = (byte) CODE_TABLE[(currentBits >> 2) & BIT_MASK_6];
        dst[21] = (byte) CODE_TABLE[(currentBits << 4) & BIT_MASK_6];
        return dst;
    }

    /**
     * Decodes the given {@code src} string into a {@link UUID}. {@code src} must be a URL and filename safe base64
     * encoded string 22 characters in length without pad characters "=".
     *
     * @param src string to be decoded into {@link UUID}
     * @return uuid decoded from the given {@code src}
     *
     * @throws NullPointerException if {@code src} is null
     * @throws IllegalArgumentException if {@code src} is not a URL and filename safe base64 encoded string without
     *                                  trailing pad characters
     */
    public static UUID decode(String src) {
        if (src == null) {
            throw new NullPointerException("src");
        }

        byte[] decoded = decodeToUuidBytes(src.getBytes(Charsets.US_ASCII));
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
        long mostSignificantBits = byteBuffer.getLong();
        long leastSignificanBits = byteBuffer.getLong();
        return new UUID(mostSignificantBits, leastSignificanBits);
    }

    private static byte[] decodeToUuidBytes(byte[] src) {
        if (src.length != 22) {
            throw new IllegalArgumentException("Invalid src byte array: " + BytesUtils.toString(src));
        }
        int srcPtr;
        int dstPtr;
        byte[] dst = new byte[16];
        // first 20 bytes, in groups of 4
        for (int i = 0; i < 5; i++) {
            //               1         2         3
            //     01234567890123456789012345678901
            // src |......||......||......||......| -> [00111111][00111111][00111111][00111111]
            // dst   |....|  |....|  |....|  |....| -> [11111111][11111111][11111111]
            srcPtr = i * 4;
            dstPtr = i * 3;
            int currentBits;
            currentBits  = INVERSE_LOOKUP.lookup(src[srcPtr  ]) << 18;
            currentBits |= INVERSE_LOOKUP.lookup(src[srcPtr+1]) << 12;
            currentBits |= INVERSE_LOOKUP.lookup(src[srcPtr+2]) << 6;
            currentBits |= INVERSE_LOOKUP.lookup(src[srcPtr+3]);
            dst[dstPtr  ] = (byte) (currentBits >> 16);
            dst[dstPtr+1] = (byte) (currentBits >>  8);
            dst[dstPtr+2] = (byte)  currentBits;
        }
        // left over 2 bytes
        //               1
        //     0123456789012345
        // src |......||......| -> [00111111][00110000]
        // dst   |....|  ||     -> [11111111]
        int currentBits = INVERSE_LOOKUP.lookup(src[20]) << 2
                        | INVERSE_LOOKUP.lookup(src[21]) >> 4;
        dst[15] = (byte) currentBits;
        return dst;
    }
}
