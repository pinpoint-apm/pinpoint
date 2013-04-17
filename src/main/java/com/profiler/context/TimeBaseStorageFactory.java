package com.profiler.context;

import com.profiler.config.ProfilerConfig;
import com.profiler.sender.DataSender;

/**
 *
 */
public class TimeBaseStorageFactory implements StorageFactory {

    private DataSender dataSender;
    private ProfilerConfig config;
//    private boolean discardEnable;
//    private int bufferSize;
//    private long discardTimeLimit;
//
//    public TimeBaseStorageFactory(DataSender dataSender, boolean discardEnable, int bufferSize, long discardTimeLimit) {
//        this.dataSender = dataSender;
//        this.discardEnable = discardEnable;
//        this.bufferSize = bufferSize;
//        this.discardTimeLimit = discardTimeLimit;
//    }

    public TimeBaseStorageFactory(DataSender dataSender, ProfilerConfig config) {
        this.dataSender = dataSender;
        this.config = config;
    }


    @Override
    public Storage createStorage() {
        TimeBaseStorage timeBaseStorage = new TimeBaseStorage();
        timeBaseStorage.setDataSender(this.dataSender);
        timeBaseStorage.setBufferSize(config.getSamplingElapsedTimeBaseBufferSize());
        timeBaseStorage.setLimitTime(config.getSamplingElapsedTimeBaseDiscardTimeLimit());
        timeBaseStorage.setDiscard(config.isSamplingElapsedTimeBaseDiscard());
        return timeBaseStorage;
    }

    @Override
    public DataSender getDataSender() {
        return this.dataSender;
    }
}
