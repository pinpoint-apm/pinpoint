package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;

import java.util.Objects;

public class Encoding implements CompositeArgument {
    public enum ENCODE {
        COMPRESSED, UNCOMPRESSED
    }
    private final ENCODE encoding;

    private Encoding(ENCODE encoding) {
        this.encoding = Objects.requireNonNull(encoding, "encoding");
    }

    public static Encoding compressed() {
        return new Encoding(ENCODE.COMPRESSED);
    }

    public static Encoding uncompressed() {
        return new Encoding(ENCODE.UNCOMPRESSED);
    }

    @Override
    public <K, V> void build(io.lettuce.core.protocol.CommandArgs<K, V> args) {
        args.add("ENCODING").add(encoding.name());
    }

    @Override
    public String toString() {
        return "ENCODING{" +
                encoding +
                '}';
    }

}
