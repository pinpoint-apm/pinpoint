package com.nhn.pinpoint.context;

import com.nhn.pinpoint.sender.DataSender;

/**
 *
 */
public interface StorageFactory {
    Storage createStorage();

    DataSender getDataSender();
}
