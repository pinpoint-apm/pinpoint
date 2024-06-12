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
package com.navercorp.pinpoint.common.server.util;


import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * @author intr3p1d
 */
public final class HashUtils {

    private HashUtils() {
    }

    private static final HashFunction HASH = Hashing.murmur3_128();

    public static Hasher newHasher() {
        return HASH.newHasher();
    }

    public static <T> String objectsToHashString(Iterable<T> objects, Funnel<T> funnel) {
        return objectsToHashCode(objects, funnel).toString();
    }

    public static <T> HashCode objectsToHashCode(Iterable<T> objects, Funnel<T> funnel) {
        Hasher hc = newHasher();
        for (T element: objects) {
            funnel.funnel(element, hc);
        }
        return hc.hash();
    }

    public static HashCode hashBytes(byte[] bytes) {
        return HASH.hashBytes(bytes);
    }

}
