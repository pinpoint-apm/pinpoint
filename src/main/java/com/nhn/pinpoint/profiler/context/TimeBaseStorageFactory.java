package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * @author emeroad
 */
public class TimeBaseStorageFactory implements StorageFactory {

    private final DataSender dataSender;
    private final int bufferSize;
    private final boolean discardEnable;
    private final long discardTimeLimit;
    private final SpanChunkFactory spanChunkFactory;

    public TimeBaseStorageFactory(DataSender dataSender, ProfilerConfig config, AgentInformation agentInformation) {
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

        this.spanChunkFactory = new SpanChunkFactory(agentInformation);
    }


    @Override
    public Storage createStorage() {
        TimeBaseStorage timeBaseStorage = new TimeBaseStorage(this.dataSender, spanChunkFactory);
        timeBaseStorage.setBufferSize(this.bufferSize);
        timeBaseStorage.setLimitTime(this.discardTimeLimit);
        timeBaseStorage.setDiscard(this.discardEnable);
        return timeBaseStorage;
    }

}
