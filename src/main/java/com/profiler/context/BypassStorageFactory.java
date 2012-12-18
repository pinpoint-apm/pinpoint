package com.profiler.context;

import com.profiler.sender.DataSender;

/**
 *
 */
public class BypassStorageFactory implements StorageFactory {
    private BypassStorage storage = new BypassStorage();

    public BypassStorageFactory(DataSender dataSender) {
        storage.setDataSender(dataSender);
    }

    @Override
    public Storage createStorage() {
        return storage;
    }
}
