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

package com.navercorp.pinpoint.common.hbase.wd;


import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 *
 */
public class RangeDoubleHash implements ByteHasher {
    public static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    private static final SaltKey SALT_KEY = ByteSaltKey.SALT;

    private final int start;
    private final int end;
    private final int maxBuckets;

    private final int secondaryMod;

    private final SaltKeyPrefix[] saltKeyPrefixes;

    private final SecondaryHashFunction secondaryHashFunction;

    public static ByteHasher ofRandom(int start, int end, int maxBuckets, int secondaryMod) {
        SecondaryHashFunction secondaryHashFunction = new SecondaryRandomHashFunction(secondaryMod);
        return new RangeDoubleHash(start, end, maxBuckets, secondaryMod, secondaryHashFunction);
    }

    public static ByteHasher ofSecondary(int start, int end, int maxBuckets, int secondaryMod, int secondaryStart, int secondaryEnd) {
        SecondaryHashFunction secondaryHashFunction = new SecondaryRangeHashFunction(secondaryStart, secondaryEnd, secondaryMod);
        return new RangeDoubleHash(start, end, maxBuckets, secondaryMod, secondaryHashFunction);
    }


    public RangeDoubleHash(int start, int end, int maxBuckets, int secondaryMod, SecondaryHashFunction secondaryHashFunction) {
        if (maxBuckets < 1 || maxBuckets > MAX_BUCKETS) {
            throw new IllegalArgumentException("maxBuckets should be in 1..256 range");
        }
        this.start = start;
        this.end = end;
        // i.e. "real" maxBuckets value = maxBuckets or maxBuckets-1
        this.maxBuckets = maxBuckets;

        this.saltKeyPrefixes = newSaltKeyPrefixes(maxBuckets);
        this.secondaryMod = secondaryMod;

        this.secondaryHashFunction = Objects.requireNonNull(secondaryHashFunction, "secondaryFunction");
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
//        int index = firstIndex(originalKey, 0);
//
//        int secondaryIndex = secondaryIndex(originalKey, 0);
//        index = secondaryModIndex(index, secondaryIndex);
//
//        return (byte) index;

        return getHashPrefix(originalKey, 0);
    }

    @Override
    public byte getHashPrefix(byte[] originalKey, int saltKeySize) {
        int index = firstIndex(originalKey, saltKeySize);

        int secondaryIndex = this.secondaryHashFunction.secondary(originalKey, saltKeySize);
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

    int firstIndex(byte[] originalKey, int saltKeySize) {
        return MathUtils.fastAbs(hashBytes(originalKey, saltKeySize)) % maxBuckets;
    }

    public interface SecondaryHashFunction {
        int secondary(byte[] originalKey, int saltKeySize);
    }

    public static class SecondaryRandomHashFunction implements SecondaryHashFunction {
        private final int mod;

        public SecondaryRandomHashFunction(int mod) {
            this.mod = mod;
        }

        @Override
        public int secondary(byte[] originalKey, int saltKeySize) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            return random.nextInt(mod);
        }
    }

    public static class SecondaryRangeHashFunction implements SecondaryHashFunction {

        private final int start;
        private final int end;
        private final int mod;

        public SecondaryRangeHashFunction(int start, int end, int mod) {
            this.start = start;
            this.end = end;
            this.mod = mod;
        }

        @Override
        public int secondary(byte[] originalKey, int saltKeySize) {
            final int hash = MathUtils.fastAbs(BytesUtils.hashBytes(originalKey, start + saltKeySize, end + saltKeySize));
            return hash % mod;
//            return hashByteByHashFunction(originalKey, saltKeySize);
        }

//        private int hashByteByHashFunction(byte[] originalKey, int saltKeySize) {
//            HashCode hashCode = hashFunction.hashBytes(originalKey, start + saltKeySize, end - start + saltKeySize);
//            return hashCode.asInt() % mod;
//        }
    }

    /** Compute hash for binary data. */
    private int hashBytes(byte[] bytes, int saltKeySize) {
//        int length = Math.min(bytes.length, end + saltKeySize);
//        return BytesUtils.hashBytes(bytes, start + saltKeySize, length);
        int length = end - start;
        HashCode hashCode = hashFunction.hashBytes(bytes, start + saltKeySize, length);
        return hashCode.asInt();
    }


    @Override
    public SaltKeyPrefix getAllPrefixes(byte[] originalKey) {
        int firstIndex = firstIndex(originalKey, 0);
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
