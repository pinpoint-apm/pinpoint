package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author emeroad
 */
public class SpanStorageFactory implements StorageFactory {

    protected final DataSender dataSender;

    public SpanStorageFactory(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.dataSender = dataSender;
    }

    @Override
    public Storage createStorage() {
        return new SpanStorage(this.dataSender);
    }
}
