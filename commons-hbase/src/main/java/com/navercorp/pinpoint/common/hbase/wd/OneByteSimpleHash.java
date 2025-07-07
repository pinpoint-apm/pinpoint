package com.navercorp.pinpoint.common.hbase.wd;

import com.navercorp.pinpoint.common.util.BytesUtils;

/**
 * Copy from sematext/HBaseWD
 */
public class OneByteSimpleHash implements ByteHasher {
    private final int mod;
    private final byte[] prefix;

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

        this.prefix = toModBytes(mod);
    }

    static byte[] toModBytes(int mod) {
        final byte[] prefix = new byte[mod];
        for (int i = 0; i < mod; i++) {
            prefix[i] = (byte) i;
        }
        return prefix;
    }


    @Override
    public byte getHashPrefix(byte[] originalKey) {
        int hash = Math.abs(BytesUtils.hashBytes(originalKey));
        return (byte) (hash % mod);
    }

    @Override
    public byte[] getAllPossiblePrefixes() {
        return prefix;
    }

    @Override
    public int getPrefixLength(byte[] adjustedKey) {
        return 1;
    }

}
