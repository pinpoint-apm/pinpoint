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
package com.navercorp.pinpoint.common.hbase.distributor;


import com.navercorp.pinpoint.common.util.MathUtils;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

import java.util.Arrays;

/**
 * Provides handy methods to distribute
 *
 * pinpoint copy and modify : https://github.com/sematext/HBaseWD/blob/master/src/main/java/com/sematext/hbase/wd/RowKeyDistributorByHashPrefix.java
 * @author Alex Baranau
 * @author emeroad
 */
public class RangeOneByteSimpleHash implements RowKeyDistributorByHashPrefix.Hasher {
    protected final int start;
    protected final int end;
    private int mod;

    // Used to minimize # of created object instances
    // Should not be changed. TODO: secure that
    private static final byte[][] PREFIXES;

    static {
        PREFIXES = new byte[256][];
        for (int i = 0; i < 256; i++) {
            PREFIXES[i] = new byte[] {(byte) i};
        }
    }

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
        long hash = MathUtils.fastAbs(hashBytes(originalKey));
        return new byte[] {(byte) (hash % mod)};
    }

    /** Compute hash for binary data. */
    private int hashBytes(byte[] bytes) {
        int min = Math.min(bytes.length, end);
        int hash = 1;
        for (int i = start; i < min; i++)
            hash = (31 * hash) + (int) bytes[i];
        return hash;
    }

    @Override
    public byte[][] getAllPossiblePrefixes() {
        return Arrays.copyOfRange(PREFIXES, 0, mod);
    }

    @Override
    public int getPrefixLength(byte[] adjustedKey) {
        return 1;
    }

    @Override
    public String getParamsToStore() {
        return String.valueOf(mod);
    }

    @Override
    public void init(String storedParams) {
        this.mod = Integer.parseInt(storedParams);
    }

    protected final int getMod() {
        return this.mod;
    }

}
