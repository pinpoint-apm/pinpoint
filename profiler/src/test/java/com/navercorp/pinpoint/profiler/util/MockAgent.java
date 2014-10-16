package com.nhn.pinpoint.profiler.util;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.List;

import org.apache.thrift.TBase;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.DummyInstrumentation;
import com.nhn.pinpoint.profiler.context.ResettableServerMetaDataHolder;
import com.nhn.pinpoint.profiler.context.storage.HoldingSpanStorageFactory;
import com.nhn.pinpoint.profiler.context.storage.StorageFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import com.nhn.pinpoint.profiler.sender.PeekableDataSender;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class MockAgent extends DefaultAgent {
    
    public static MockAgent of(String configPath) throws IOException {
        ProfilerConfig profilerConfig = new ProfilerConfig();
        String path = MockAgent.class.getClassLoader().getResource(configPath).getPath();
        profilerConfig.readConfigFile(path);
        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        
        return new MockAgent("", profilerConfig);
    }

    public MockAgent(String agentArgs, ProfilerConfig profilerConfig) {
        this(agentArgs, new DummyInstrumentation(), profilerConfig);
    }

    public MockAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        super(agentArgs, instrumentation, profilerConfig);
    }

    @Override
    protected DataSender createUdpDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new PeekableDataSender<TBase<?, ?>>();
    }

    public PeekableDataSender<?> getPeekableSpanDataSender() {
        DataSender spanDataSender = getSpanDataSender();
        if (spanDataSender instanceof PeekableDataSender) {
            return (PeekableDataSender<?>)getSpanDataSender();
        } else {
            throw new IllegalStateException("UdpDataSender must be an instance of a PeekableDataSender. Found : " + spanDataSender.getClass().getName());
        }
    }

    @Override
    protected StorageFactory createStorageFactory() {
        return new HoldingSpanStorageFactory(getSpanDataSender());
    }

    @Override
    protected PinpointSocketFactory createPinpointSocketFactory() {
        return null;
    }

    @Override
    protected PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
        return null;
    }

    @Override
    protected PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory, boolean useMessageListener) {
        return null;
    }

    @Override
    protected EnhancedDataSender createTcpDataSender(PinpointSocket socket) {
        return new LoggingDataSender();
    }

    @Override
    protected ServerMetaDataHolder createServerMetaDataHolder() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        return new ResettableServerMetaDataHolder(vmArgs);
    }

}
