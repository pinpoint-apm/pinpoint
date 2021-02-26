package com.navercorp.pinpoint.profiler.jdbc;

public class HexBytesConverter extends BytesConverter {

    // for uuid
    public static final int BYTES_FORMAT_MAX_SIZE = 16;

    private final int bytesFormatMaxSize;

    public HexBytesConverter() {
        this(BYTES_FORMAT_MAX_SIZE);
    }


    public HexBytesConverter(int bytesFormatMaxSize) {
        this.bytesFormatMaxSize = bytesFormatMaxSize;
    }

    @Override
    protected String convert(byte[] bytes) {
        return HexUtils.toHexString(bytes, bytesFormatMaxSize);
    }

}