package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * @author emeroad
 */
public class BufferedStorageFactory implements StorageFactory {

    private final DataSender dataSender;
    private final int bufferSize;
    private final SpanChunkFactory spanChunkFactory;

    public BufferedStorageFactory(DataSender dataSender, ProfilerConfig config, AgentInformation agentInformation) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }
        this.dataSender = dataSender;

        this.bufferSize = config.getSamplingElapsedTimeBaseBufferSize();

        this.spanChunkFactory = new SpanChunkFactory(agentInformation);
    }


    @Override
    public Storage createStorage() {
        BufferedStorage bufferedStorage = new BufferedStorage(this.dataSender, spanChunkFactory, this.bufferSize);
        return bufferedStorage;
    }

    @Override
    public String toString() {
        return "BufferedStorageFactory{" +
                "bufferSize=" + bufferSize +
                ", dataSender=" + dataSender +
                '}';
    }
}
