package com.navercorp.pinpoint.common.hbase.wd;

/**
 * Copy from sematext/HBaseWD
 */
public class OneByteSimpleHash implements Hasher {
    private final int mod;

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
    }


    @Override
    public byte[] getHashPrefix(byte[] originalKey) {
        long hash = Math.abs(WdUtils.hashBytes(originalKey));
        return new byte[]{(byte) (hash % mod)};
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
