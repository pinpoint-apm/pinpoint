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
        byte[] allPrefixes = hasher.getAllPossiblePrefixes(originalKey);

        byte[][] keys = new byte[allPrefixes.length][];
        for (int i = 0; i < allPrefixes.length; i++) {
            keys[i] = BytesUtils.add(allPrefixes[i], originalKey);
        }

        return keys;
    }

}
