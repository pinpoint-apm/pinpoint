package com.navercorp.pinpoint.common.hbase.wd;

/**
 * Copy from sematext/HBaseWD
 */
public interface Hasher extends Parametrizable {
    byte[] getHashPrefix(byte[] originalKey);

    byte[][] getAllPossiblePrefixes();

    int getPrefixLength(byte[] adjustedKey);
}
