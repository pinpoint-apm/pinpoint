package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.protocol.ChunkSize;
import com.navercorp.pinpoint.redis.timeseries.protocol.DuplicatePolicy;
import com.navercorp.pinpoint.redis.timeseries.protocol.Ignore;
import com.navercorp.pinpoint.redis.timeseries.protocol.OnDuplicate;
import com.navercorp.pinpoint.redis.timeseries.protocol.Retention;
import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

public class TsAddArgs implements CompositeArgument {
    private Retention retention;
    private DuplicatePolicy duplicatePolicy;
    private OnDuplicate onDuplicate;
    private ChunkSize chunkSize;

    private Ignore ignore;


    public TsAddArgs() {
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        if (retention != null) {
            retention.build(args);
        }
        if (duplicatePolicy != null) {
            duplicatePolicy.build(args);
        }
        if (onDuplicate != null) {
            onDuplicate.build(args);
        }
        if (chunkSize != null) {
            chunkSize.build(args);
        }
        if  (ignore != null) {
            ignore.build(args);
        }
    }

    public TsAddArgs retention(Retention retention) {
        this.retention = retention;
        return self();
    }

    public TsAddArgs duplicatePolicy(DuplicatePolicy duplicatePolicy) {
        this.duplicatePolicy = duplicatePolicy;
        return self();
    }

    public TsAddArgs onDuplicate(OnDuplicate onDuplicate) {
        this.onDuplicate = onDuplicate;
        return self();
    }

    public TsAddArgs chunkSize(int chunkSize) {
        this.chunkSize = ChunkSize.of(chunkSize);
        return self();
    }

    public TsAddArgs ignore(long ignoreMaxTimediff, long ignoreMaxValDiff) {
        this.ignore = new Ignore(ignoreMaxTimediff, ignoreMaxValDiff);
        return self();
    }

    private TsAddArgs self()  {
        return this;
    }
}
