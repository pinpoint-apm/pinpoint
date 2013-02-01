package com.profiler.context;

import com.profiler.sender.DataSender;

/**
 *
 */
public interface StorageFactory {
    Storage createStorage();

    DataSender getDataSender();
}
