package com.navercorp.pinpoint.common.hbase.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;

public class BufferedMutatorConfiguration {

    @Value("${collector.batchwrite.enable:true}")
    private boolean batchWriter = true;

    @Value("${collector.batchwrite.timertick:100}")
    private long writeBufferPeriodicFlushTimerTickMs = 100;

    @Value("${collector.batchwrite.writebuffer.size:5012}")
    private long writeBufferSize = 1024 * 5;

    @Value("${collector.batchwrite.autoflush:false}")
    private boolean autoFlush;

    // for OOM prevent
    @Value("${collector.batchwrite.writebuffer.heaplimit:100MB}")
    private DataSize writeBufferHeapLimit = DataSize.ofMegabytes(100);

    public boolean isBatchWriter() {
        return batchWriter;
    }

    public long getWriteBufferPeriodicFlushTimerTickMs() {
        return writeBufferPeriodicFlushTimerTickMs;
    }

    public long getWriteBufferSize() {
        return writeBufferSize;
    }

    public long getWriteBufferHeapLimit() {
        return writeBufferHeapLimit.toBytes();
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    @Override
    public String toString() {
        return "BufferedMutatorConfiguration{" +
                "batchWriter=" + batchWriter +
                ", writeBufferPeriodicFlushTimerTickMs=" + writeBufferPeriodicFlushTimerTickMs +
                ", writeBufferSize=" + writeBufferSize +
                ", autoFlush=" + autoFlush +
                ", writeBufferHeapLimit=" + writeBufferHeapLimit +
                '}';
    }
}
