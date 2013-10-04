package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 *
 */
public interface StorageFactory {
    Storage createStorage();
}
