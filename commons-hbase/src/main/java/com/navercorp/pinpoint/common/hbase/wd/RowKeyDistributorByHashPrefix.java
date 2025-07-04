package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Copy from sematext/HBaseWD
 * Provides handy methods to distribute
 *
 * @author Alex Baranau
 */
public class RowKeyDistributorByHashPrefix extends AbstractRowKeyDistributor {
    private static final String DELIM = "--";
    private Hasher hasher;

    /** Constructor reflection. DO NOT USE */
    public RowKeyDistributorByHashPrefix() {
    }

    public RowKeyDistributorByHashPrefix(Hasher hasher) {
        this.hasher = hasher;
    }

    @Override
    public byte[] getDistributedKey(byte[] originalKey) {
        return Bytes.add(hasher.getHashPrefix(originalKey), originalKey);
    }

    @Override
    public byte[] getOriginalKey(byte[] adjustedKey) {
        int prefixLength = hasher.getPrefixLength(adjustedKey);
        if (prefixLength > 0) {
            return Bytes.tail(adjustedKey, adjustedKey.length - prefixLength);
        } else {
            return adjustedKey;
        }
    }

    @Override
    public byte[][] getAllDistributedKeys(byte[] originalKey) {
        byte[][] allPrefixes = hasher.getAllPossiblePrefixes();
        byte[][] keys = new byte[allPrefixes.length][];
        for (int i = 0; i < allPrefixes.length; i++) {
            keys[i] = Bytes.add(allPrefixes[i], originalKey);
        }

        return keys;
    }

    @Override
    public String getParamsToStore() {
        String hasherParamsToStore = hasher.getParamsToStore();
        return hasher.getClass().getName() + DELIM + (hasherParamsToStore == null ? "" : hasherParamsToStore);
    }

    @Override
    public void init(String params) {
        String[] parts = params.split(DELIM, 2);
        try {
            this.hasher = (Hasher) Class.forName(parts[0]).newInstance();
            this.hasher.init(parts[1]);
        } catch (Exception e) {
            throw new RuntimeException("RoKeyDistributor initialization failed", e);
        }
    }
}
