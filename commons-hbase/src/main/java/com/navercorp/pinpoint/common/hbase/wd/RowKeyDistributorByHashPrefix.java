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

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

/**
 * Copy from sematext/HBaseWD
 * Provides handy methods to distribute
 *
 * @author Alex Baranau
 */
public class RowKeyDistributorByHashPrefix implements RowKeyDistributor {
    private final ByteHasher hasher;

    public RowKeyDistributorByHashPrefix(ByteHasher hasher) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    @Override
    public ByteHasher getByteHasher() {
        return hasher;
    }

    @Override
    public int getSaltKeySize() {
        return hasher.getSaltKey().size();
    }


    @Override
    public byte[] getDistributedKey(byte[] originalKey) {
        byte hashPrefix = hasher.getHashPrefix(originalKey);
        return BytesUtils.add(hashPrefix, originalKey);
    }


    @Override
    public byte[] getOriginalKey(byte[] adjustedKey) {
        int prefixLength = hasher.getPrefixLength(adjustedKey);
        if (prefixLength > 0) {
            return Bytes.tail(adjustedKey, adjustedKey.length - 1);
        } else {
            return adjustedKey;
        }
    }

    @Override
    public byte[][] getAllDistributedKeys(byte[] originalKey) {
        SaltKeyPrefix allPrefixes = hasher.getAllPrefixes(originalKey);
        final int size = allPrefixes.size();
        byte[][] keys = new byte[size][];
        for (int i = 0; i < size; i++) {
            byte prefix = allPrefixes.getPrefix(i, originalKey);
            keys[i] = BytesUtils.add(prefix, originalKey);
        }
        return keys;
    }

}
