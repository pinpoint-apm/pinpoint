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

/**
 * Copy from sematext/HBaseWD
 */
public class OneByteSimpleHash implements ByteHasher {
    private static final SaltKey SALT_KEY = ByteSaltKey.SALT;

    private final int mod;
    private final SaltKeyPrefix saltKeyPrefix;
    /**
     * Creates a new instance of this class.
     *
     * @param maxBuckets max buckets number, should be in 1...255 range
     */
    public OneByteSimpleHash(int maxBuckets) {
        if (maxBuckets < 1 || maxBuckets > 256) {
            throw new IllegalArgumentException("maxBuckets should be in 1..256 range");
        }
        // i.e. "real" maxBuckets value = maxBuckets or maxBuckets-1
        this.mod = maxBuckets;
        this.saltKeyPrefix = new ModSaltKeyPrefix(mod);
    }



    @Override
    public byte getHashPrefix(byte[] originalKey) {
        return getHashPrefix(originalKey, 0);
    }

    @Override
    public byte getHashPrefix(byte[] originalKey, int saltKeySize) {
        int hash = MathUtils.fastAbs(BytesUtils.hashBytes(originalKey, saltKeySize, originalKey.length));
        return (byte) (hash % mod);
    }

    @Override
    public byte[] writeSaltKey(byte[] saltedKey) {
        saltedKey[0] = getHashPrefix(saltedKey, SALT_KEY.size());
        return saltedKey;
    }

    @Override
    public SaltKeyPrefix getAllPrefixes(byte[] originalKey) {
        return saltKeyPrefix;
    }

    @Override
    public int getPrefixLength(byte[] adjustedKey) {
        return SALT_KEY.size();
    }

    @Override
    public SaltKey getSaltKey() {
        return SALT_KEY;
    }

}
