package com.navercorp.pinpoint.profiler.jdbc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

public class JdbcContextConfig {
    public static final String BYTES_FORMAT = "profiler.jdbc.preparedstatement.bytes.format";

    public enum ByteFormat {
        raw, hex;
    }

    private final ByteFormat byteFormat;

    public JdbcContextConfig(ProfilerConfig profilerConfig) {
        Objects.requireNonNull(profilerConfig, "profilerConfig");

        final String byteFormat = profilerConfig.readString(BYTES_FORMAT, ByteFormat.raw.name());
        this.byteFormat = ByteFormat.valueOf(byteFormat);
    }

    public ByteFormat getByteFormat() {
        return byteFormat;
    }

}
