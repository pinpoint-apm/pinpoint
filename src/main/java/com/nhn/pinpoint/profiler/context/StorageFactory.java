package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * @author emeroad
 */
public interface StorageFactory {
    Storage createStorage();
}
