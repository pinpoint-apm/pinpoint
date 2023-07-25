package com.navercorp.pinpoint.profiler.metadata;

import java.util.Arrays;

public class UidParsingResult extends ParsingResultInternal<byte[]> {
    private byte[] uid;

    public UidParsingResult(String originalSql) {
        super(originalSql);
    }

    @Override
    public byte[] getId() {
        return this.uid;
    }

    @Override
    public boolean setId(byte[] uid) {
        clearOriginalSql();

        if (this.uid == null && uid != null) {
            this.uid = uid;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UidParsingResult{" +
                "sql=" + getSql() +
                ", output=" + getOutput() +
                ", uid=" + Arrays.toString(uid) +
                '}';
    }
}
