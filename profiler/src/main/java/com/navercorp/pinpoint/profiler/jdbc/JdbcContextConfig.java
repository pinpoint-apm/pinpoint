package com.navercorp.pinpoint.profiler.jdbc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

public class JdbcContextConfig {
    public static final String BYTES_FORMAT = "profiler.jdbc.preparedstatement.bytes.format";
    public static final String MAX_WIDTH = "profiler.jdbc.format.maxwidth";

    public enum ByteFormat {
        raw, hex
    }

    private final ByteFormat byteFormat;
    private final int maxWidth;

    public JdbcContextConfig(ProfilerConfig profilerConfig) {
        Objects.requireNonNull(profilerConfig, "profilerConfig");

        final String byteFormat = profilerConfig.readString(BYTES_FORMAT, ByteFormat.raw.name());
        this.byteFormat = ByteFormat.valueOf(byteFormat);
        this.maxWidth = profilerConfig.readInt(MAX_WIDTH, 32);
    }

    public ByteFormat getByteFormat() {
        return byteFormat;
    }

    public int getMaxWidth() {
        return maxWidth;
    }
}
