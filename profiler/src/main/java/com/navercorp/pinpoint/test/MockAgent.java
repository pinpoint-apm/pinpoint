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
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TBase;

import com.google.common.base.Objects;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.plugin.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class MockAgent extends DefaultAgent implements PluginTestVerifier {
   
    public static MockAgent of(String configPath) throws IOException {
        String path = MockAgent.class.getClassLoader().getResource(configPath).getPath();
        ProfilerConfig profilerConfig = ProfilerConfig.load(path);
        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        
        return new MockAgent("", profilerConfig);
    }
    
    public static MockAgent of(ProfilerConfig config) {
        return new MockAgent("", config);
    }

    public MockAgent(String agentArgs, ProfilerConfig profilerConfig) {
        this(agentArgs, new DummyInstrumentation(), profilerConfig);
    }

    public MockAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        this(agentArgs, instrumentation, profilerConfig, new URL[0]);
    }
    
    public MockAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig, URL[] pluginJars) {
        super(agentArgs, instrumentation, profilerConfig, pluginJars);
        
        PluginTestVerifierHolder.setInstance(this);
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
    
    @Override
    public void verifyServerType(ServiceType serviceType) {
        short actualType = getAgentInformation().getServerType();
        
        if (serviceType.getCode() != actualType) {
            throw new AssertionError("Expected server type: " + serviceType.getName() + "[" + serviceType.getCode() + "] but was [" + actualType + "]");
        };
    }
    
    @Override
    public void verifySpanCount(int expected) {
        int actual = getPeekableSpanDataSender().size();
        
        if (expected != actual) {
            throw new AssertionError("Expected count: " + expected + ", actual: " + actual);
        }
    }
    
    public static String toString(Span span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        
        boolean first = true;
        
        for (TAnnotation a : span.getAnnotations()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            
            builder.append(toString(a));
        }
        
        builder.append("])");
        
        return builder.toString();
    }

    public static String toString(SpanEvent span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        
        boolean first = true;
        
        for (TAnnotation a : span.getAnnotations()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            
            builder.append(toString(a));
        }
        
        builder.append("])");
        
        return builder.toString();
    }

    private static String toString(TAnnotation a) {
        return a.getKey() + "=" + a.getValue().getFieldValue();
    }
    
    public static String toString(short serviceCode, ExpectedAnnotation...annotations) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(serviceCode);
        builder.append(", ");
        builder.append(Arrays.deepToString(annotations));
        builder.append(")");
        
        return builder.toString();
    }

    @Override
    public void verifySpan(ServiceType serviceType, ExpectedAnnotation... annotations) {
        Object obj = getPeekableSpanDataSender().poll();
        short code = serviceType.getCode();
        
        if (!(obj instanceof Span)) {
            throw new AssertionError("Expected an instance of Span but was " + obj.getClass().getName() +". expected: " + toString(code, annotations) + ", was: " + obj);
        }
        
        Span span = (Span)obj;
        
        if (code != span.getServiceType()) {
            throw new AssertionError("Expected a Span with serviceType[" + code + "] but was [" + span.getServiceType() + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        List<TAnnotation> actualAnnotations = span.getAnnotations();
        int len = annotations.length;
        int actualLen = actualAnnotations.size();
        
        if (actualLen != len) {
            throw new AssertionError("Expected a Span with [" + len + "] annotations but was [" + actualLen + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        for (int i = 0; i < len; i++) {
            ExpectedAnnotation expect = annotations[i];
            TAnnotation actual = actualAnnotations.get(i);
            
            if (expect.getKey() != actual.getKey() || !Objects.equal(expect.getValue(), actual.getValue().getFieldValue())) {
                throw new AssertionError("Expected a Span with " + i + "th annotation [" + expect + "] but was [" + toString(actual) + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
            }
        }
    }

    @Override
    public void verifySpanEvent(ServiceType serviceType, ExpectedAnnotation... annotations) {
        Object obj = getPeekableSpanDataSender().poll();
        short code = serviceType.getCode();
        
        if (!(obj instanceof SpanEvent)) {
            throw new AssertionError("Expected an instance of SpanEvent but was " + obj.getClass().getName() +". expected: " + toString(code, annotations) + ", was: " + obj);
        }
        
        SpanEvent span = (SpanEvent)obj;
        
        if (code != span.getServiceType()) {
            throw new AssertionError("Expected a SpanEvent with serviceType[" + code + "] but was [" + span.getServiceType() + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        List<TAnnotation> actualAnnotations = span.getAnnotations();
        int actualLen = actualAnnotations.size();
        int len = annotations.length;
        
        if (actualLen != len) {
            throw new AssertionError("Expected a SpanEvent with [" + len + "] annotations but was [" + actualLen + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        for (int i = 0; i < len; i++) {
            ExpectedAnnotation expect = annotations[i];
            TAnnotation actual = actualAnnotations.get(i);
            
            if (expect.getKey() != actual.getKey() || !Objects.equal(expect.getValue(), actual.getValue().getFieldValue())) {
                throw new AssertionError("Expected a SpanEvent with " + i + "th annotation [" + expect + "] but was [" + toString(actual) + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
            }
        }
    }

    @Override
    public void clearSpans() {
        getPeekableSpanDataSender().clear();
    }
}
