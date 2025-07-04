/**
 * Copyright 2010 Sematext International
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


import com.navercorp.pinpoint.common.util.MathUtils;

/**
 * Copy from sematext/HBaseWD
 * Provides handy methods to distribute
 *
 * @author Alex Baranau
 * @author emeroad
 */
public class RangeOneByteSimpleHash implements Hasher {
    protected final int start;
    protected final int end;
    private final int mod;


    public RangeOneByteSimpleHash(int start, int end, int maxBuckets) {
        if (maxBuckets < 1 || maxBuckets > 256) {
            throw new IllegalArgumentException("maxBuckets should be in 1..256 range");
        }

        this.start = start;
        this.end = end;
        // i.e. "real" maxBuckets value = maxBuckets or maxBuckets-1
        this.mod = maxBuckets;
    }

    @Override
    public byte[] getHashPrefix(byte[] originalKey) {
        int hash = MathUtils.fastAbs(hashBytes(originalKey));
        return new byte[] {(byte) (hash % mod)};
    }

    /** Compute hash for binary data. */
    private int hashBytes(byte[] bytes) {
        int length = Math.min(bytes.length, end);
        return WdUtils.hashBytes(bytes, start, length);
    }

    @Override
    public byte[][] getAllPossiblePrefixes() {
        return WdUtils.OneByte.prefixes(0, mod);
    }

    @Override
    public int getPrefixLength(byte[] adjustedKey) {
        return 1;
    }

}
