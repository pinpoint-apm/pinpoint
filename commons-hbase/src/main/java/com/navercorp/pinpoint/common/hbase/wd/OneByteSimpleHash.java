package com.navercorp.pinpoint.common.hbase.wd;

import java.util.Arrays;

/**
 * Copy from sematext/HBaseWD
 */
public class OneByteSimpleHash implements Hasher {
    private int mod;

    /**
     * For reflection, do NOT use it.
     */
    public OneByteSimpleHash() {
    }

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

    // Used to minimize # of created object instances
    // Should not be changed. TODO: secure that
    private static final byte[][] PREFIXES;

    static {
        PREFIXES = new byte[256][];
        for (int i = 0; i < 256; i++) {
            PREFIXES[i] = new byte[]{(byte) i};
        }
    }

    @Override
    public byte[] getHashPrefix(byte[] originalKey) {
        long hash = Math.abs(hashBytes(originalKey));
        return new byte[]{(byte) (hash % mod)};
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

    /**
     * Compute hash for binary data.
     */
    private static int hashBytes(byte[] bytes) {
        int hash = 1;
        for (int i = 0; i < bytes.length; i++) {
            hash = (31 * hash) + (int) bytes[i];
        }
        return hash;
    }
}
