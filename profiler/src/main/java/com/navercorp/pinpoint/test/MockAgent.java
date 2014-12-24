/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.List;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class MockAgent extends DefaultAgent {
    
    public static MockAgent of(String configPath) throws IOException {
        String path = MockAgent.class.getClassLoader().getResource(configPath).getPath();
        ProfilerConfig profilerConfig = ProfilerConfig.load(path);
        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        
        return new MockAgent("", "", profilerConfig);
    }
    
    public static MockAgent of(ProfilerConfig config) {
        return new MockAgent("", "", config);
    }

    public MockAgent(String agentPath, String agentArgs, ProfilerConfig profilerConfig) {
        this(agentPath, agentArgs, new DummyInstrumentation(), profilerConfig);
    }

    public MockAgent(String agentPath, String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        super(agentPath, agentArgs, instrumentation, profilerConfig);
    }

    @Override
    protected DataSender createUdpStatDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new PeekableDataSender<TBase<?, ?>>();
    }

    @Override
    protected DataSender createUdpSpanDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
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
    protected PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
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
