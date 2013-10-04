package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 *
 */
public class TimeBaseStorageFactory implements StorageFactory {

    private final DataSender dataSender;
    private final int bufferSize;
    private final boolean discardEnable;
    private final long discardTimeLimit;

    public TimeBaseStorageFactory(DataSender dataSender, ProfilerConfig config) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }
        this.dataSender = dataSender;

        this.bufferSize = config.getSamplingElapsedTimeBaseBufferSize();
        this.discardEnable = config.isSamplingElapsedTimeBaseDiscard();
        this.discardTimeLimit = config.getSamplingElapsedTimeBaseDiscardTimeLimit();
    }


    @Override
    public Storage createStorage() {
        TimeBaseStorage timeBaseStorage = new TimeBaseStorage(this.dataSender);
        timeBaseStorage.setBufferSize(this.bufferSize);
        timeBaseStorage.setLimitTime(this.discardTimeLimit);
        timeBaseStorage.setDiscard(this.discardEnable);
        return timeBaseStorage;
    }

}
