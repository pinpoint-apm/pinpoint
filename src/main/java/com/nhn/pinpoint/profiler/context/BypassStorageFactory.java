package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 *
 */
public class BypassStorageFactory implements StorageFactory {
    // 현재 해당 storage는 상태가 없어서 재사용해도 된다. 만약 상태가 존재하게 변경된다면 수정해야 된다.
    private BypassStorage storage ;
    private DataSender dataSender;

    public BypassStorageFactory(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.dataSender = dataSender;
        storage = new BypassStorage(dataSender);
    }

    @Override
    public Storage createStorage() {
        return storage;
    }

    @Override
    public DataSender getDataSender() {
        return dataSender;
    }
}
