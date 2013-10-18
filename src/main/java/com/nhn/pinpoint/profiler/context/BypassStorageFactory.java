package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * 개별로 보내는 일은 없도록 함.
 */
@Deprecated
public class BypassStorageFactory implements StorageFactory {
    // 현재 해당 storage는 상태가 없어서 재사용해도 된다. 만약 상태가 존재하게 변경된다면 수정해야 된다.
    private final BypassStorage storage ;

    public BypassStorageFactory(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        storage = new BypassStorage(dataSender);
    }

    @Override
    public Storage createStorage() {
        return storage;
    }

}
