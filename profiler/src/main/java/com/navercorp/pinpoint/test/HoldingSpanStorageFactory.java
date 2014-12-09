package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author hyungil.jeong
 */
public class HoldingSpanStorageFactory implements StorageFactory {

    private final PeekableDataSender<?> dataSender;

    public HoldingSpanStorageFactory(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (dataSender instanceof PeekableDataSender) {
            this.dataSender = (PeekableDataSender<?>)dataSender;
        } else {
            throw new IllegalArgumentException("dataSender must be an instance of PeekableDataSender.");
        }
    }

    @Override
    public Storage createStorage() {
        return new HoldingSpanStorage(this.dataSender);
    }

}
