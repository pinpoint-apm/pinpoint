package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

/**
 * Copy from sematext/HBaseWD
 * Provides handy methods to distribute
 *
 * @author Alex Baranau
 */
public class RowKeyDistributorByHashPrefix implements RowKeyDistributor {
    private final Hasher hasher;

    public RowKeyDistributorByHashPrefix(Hasher hasher) {
        this.hasher = Objects.requireNonNull(hasher, "hasher");
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
        byte[][] allPrefixes = hasher.getAllPossiblePrefixes(originalKey);
        byte[][] keys = new byte[allPrefixes.length][];
        for (int i = 0; i < allPrefixes.length; i++) {
            keys[i] = Bytes.add(allPrefixes[i], originalKey);
        }

        return keys;
    }

}
