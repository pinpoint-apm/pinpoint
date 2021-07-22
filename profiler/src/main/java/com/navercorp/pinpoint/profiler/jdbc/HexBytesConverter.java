package com.navercorp.pinpoint.profiler.jdbc;

public class HexBytesConverter extends BytesConverter {

    // for uuid
    public static final int BYTES_FORMAT_MAX_SIZE = 16;


    public HexBytesConverter() {
        this(BYTES_FORMAT_MAX_SIZE);
    }

    public HexBytesConverter(int maxWidth) {
        super(maxWidth);
    }

    @Override
    protected String convert(byte[] bytes) {
        return HexUtils.toHexString(bytes, maxWidth);
    }

}