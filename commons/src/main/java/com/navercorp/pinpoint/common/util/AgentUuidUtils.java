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

import java.util.UUID;

/**
 * @author HyunGil Jeong
 */
public class AgentUuidUtils {

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
        return UuidUtils.encode(uuidString);
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
        return UuidUtils.encode(uuid);
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
        return UuidUtils.decode(src);
    }

}
