package com.navercorp.pinpoint.common.hbase.wd;

/**
 * Copy from sematext/HBaseWD
 */
public interface Hasher {
    byte[] getHashPrefix(byte[] originalKey);

    byte[][] getAllPossiblePrefixes();

    default byte[][] getAllPossiblePrefixes(byte[] originalKey) {
        return getAllPossiblePrefixes();
    }

    int getPrefixLength(byte[] adjustedKey);

}
