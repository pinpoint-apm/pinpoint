package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

public class ChunkSize implements CompositeArgument  {

    public static ChunkSize of(int chunkSize) {
        return new ChunkSize(chunkSize);
    }

    ChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    protected final int chunkSize;

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("CHUNK_SIZE").add(chunkSize);

    }
}
