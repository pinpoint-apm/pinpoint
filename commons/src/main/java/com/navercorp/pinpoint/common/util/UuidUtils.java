/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class UuidUtils {
    
    public static final UUID EMPTY = new UUID(0, 0);

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

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
        Objects.requireNonNull(uuidString, "uuidString");

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
        Objects.requireNonNull(uuid, "uuid");

        final byte[] bytes = UuidUtils.toBytes(uuid);

        byte[] encode = ENCODER.encode(bytes);
        return new String(encode, StandardCharsets.US_ASCII);
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
        Objects.requireNonNull(src, "src");
        byte[] bytes = src.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length != 22) {
            throw new IllegalArgumentException("Invalid src byte array: " + src);
        }

        byte[] decoded = Base64.getUrlDecoder().decode(bytes);

        return UuidUtils.fromBytes(decoded);
    }

    public static UUID fromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length != 16) {
            throw new IllegalArgumentException("Invalid bytes length: " + bytes.length);
        }

        long mostSigBits = BytesUtils.bytesToLong(bytes, 0);
        long leastSigBits = BytesUtils.bytesToLong(bytes, BytesUtils.LONG_BYTE_LENGTH);
        return new UUID(mostSigBits, leastSigBits);
    }

    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        byte[] bytes = new byte[16];
        BytesUtils.writeLong(uuid.getMostSignificantBits(), bytes, 0);
        BytesUtils.writeLong(uuid.getLeastSignificantBits(), bytes, BytesUtils.LONG_BYTE_LENGTH);
        return bytes;
    }

    public static UUID add(UUID a, int b) {
        long mostSigBits = a.getMostSignificantBits();
        long leastSigBits = a.getLeastSignificantBits();
        leastSigBits += b;
        if (leastSigBits - b < 0 && leastSigBits >= 0) {
            mostSigBits++;
        }
        return new UUID(mostSigBits, leastSigBits);
    }

    public static UUID createV4() {
        return UUID.randomUUID();
    }

}
