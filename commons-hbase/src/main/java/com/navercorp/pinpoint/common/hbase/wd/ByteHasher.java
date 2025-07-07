package com.navercorp.pinpoint.common.hbase.wd;

public interface ByteHasher {

    byte getHashPrefix(byte[] originalKey);

    byte[] getAllPossiblePrefixes();

    default byte[] getAllPossiblePrefixes(byte[] originalKey) {
        return getAllPossiblePrefixes();
    }

    int getPrefixLength(byte[] adjustedKey);

}
