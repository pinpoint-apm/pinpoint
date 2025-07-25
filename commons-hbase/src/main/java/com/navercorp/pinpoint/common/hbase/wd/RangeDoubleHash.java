/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.wd;


import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.ThreadLocalRandom;


/**
 *
 */
public class RangeDoubleHash implements ByteHasher {
    private static final SaltKey SALT_KEY = ByteSaltKey.SALT;

    private final int start;
    private final int end;
    private final int maxBuckets;

    private final int secondaryMod;

    private final SaltKeyPrefix[] saltKeyPrefixes;

    public RangeDoubleHash(int start, int end, int maxBuckets, int secondaryMod) {
        if (maxBuckets < 1 || maxBuckets > MAX_BUCKETS) {
            throw new IllegalArgumentException("maxBuckets should be in 1..256 range");
        }
        this.start = start;
        this.end = end;
        // i.e. "real" maxBuckets value = maxBuckets or maxBuckets-1
        this.maxBuckets = maxBuckets;

        this.secondaryMod = secondaryMod;
        this.saltKeyPrefixes = newSaltKeyPrefixes(maxBuckets);
    }

    private SaltKeyPrefix[] newSaltKeyPrefixes(int maxBuckets) {
        final SaltKeyPrefix[] saltKeyPrefixes = new SaltKeyPrefix[maxBuckets];
        for (int i = 0; i < saltKeyPrefixes.length; i++) {
            saltKeyPrefixes[i] = new DoubleHashKeyPrefix(i);
        }
        return saltKeyPrefixes;
    }


    @Override
    public byte getHashPrefix(byte[] originalKey) {
        int index = firstIndex(originalKey);

        int secondaryIndex = secondaryIndex(originalKey);
        index = secondaryModIndex(index, secondaryIndex);

        return (byte) index;
    }

    @Override
    public byte[] writeSaltKey(byte[] saltedKey) {
        saltedKey[0] = getHashPrefix(saltedKey, SALT_KEY.size());
        return saltedKey;
    }

    int secondaryModIndex(int index, int secondaryIndex) {
        return (index + secondaryIndex) % maxBuckets;
    }

    int firstIndex(byte[] originalKey) {
        return MathUtils.fastAbs(hashBytes(originalKey)) % maxBuckets;
    }

    protected int secondaryIndex(byte[] originalKey) {
        // Random distribution
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextInt(secondaryMod);
    }

    /** Compute hash for binary data. */
    private int hashBytes(byte[] bytes) {
        int length = Math.min(bytes.length, end);
//        HashCode hashCode = Hashing.murmur3_32().hashBytes(bytes, start, length);
//        return hashCode.asInt();
        return BytesUtils.hashBytes(bytes, start, length);
    }


    @Override
    public SaltKeyPrefix getAllPrefixes(byte[] originalKey) {
        int firstIndex = firstIndex(originalKey);
        return saltKeyPrefixes[firstIndex];
    }

    @Override
    public int getPrefixLength(byte[] adjustedKey) {
        return SALT_KEY.size();
    }

    public SaltKey getSaltKey() {
        return SALT_KEY;
    }

    class DoubleHashKeyPrefix implements SaltKeyPrefix {

        private final int firstIndex;
        public DoubleHashKeyPrefix(int firstIndex) {
            this.firstIndex = firstIndex;
        }

        @Override
        public int size() {
            return secondaryMod;
        }

        @Override
        public byte getPrefix(int index, byte[] originalKey) {
            return (byte) secondaryModIndex(firstIndex, index);
        }
    }
}
