package com.nhn.pinpoint.profiler.util;

import java.lang.instrument.Instrumentation;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.DummyInstrumentation;
import com.nhn.pinpoint.profiler.context.storage.ReadableSpanStorageFactory;
import com.nhn.pinpoint.profiler.context.storage.StorageFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class MockAgent extends DefaultAgent {

    public MockAgent(String agentArgs, ProfilerConfig profilerConfig) {
        this(agentArgs, new DummyInstrumentation(), profilerConfig);
    }

    public MockAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        super(agentArgs, instrumentation, profilerConfig);
    }

    @Override
    protected DataSender createUdpDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return super.createUdpDataSender(port, threadName, writeQueueSize, timeout, sendBufferSize);
    }

    @Override 
    protected StorageFactory createStorageFactory() {
		return new ReadableSpanStorageFactory();
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
    protected EnhancedDataSender createTcpDataSender(PinpointSocket socket) {
        return new LoggingDataSender();
    }
    
}
