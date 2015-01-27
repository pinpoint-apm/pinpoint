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
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TBase;

import com.google.common.base.Objects;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
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
        return new TestTcpDataSender();
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
        appendAnnotations(builder, span.getAnnotations());
        builder.append("])");
        
        return builder.toString();
    }

    public static String toString(SpanEvent span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        appendAnnotations(builder, span.getAnnotations());
        builder.append("])");
        
        return builder.toString();
    }

    private static void appendAnnotations(StringBuilder builder, List<TAnnotation> annotations) {
        boolean first = true;
        
        if (annotations != null) {
            for (TAnnotation a : annotations) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                
                builder.append(toString(a));
            }
        }
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
        
        if (obj == null) {
            throw new AssertionError("No Span. expected: " + toString(code, annotations));
        }
        
        if (!(obj instanceof Span)) {
            throw new AssertionError("Expected an instance of Span but was " + obj.getClass().getName() +". expected: " + toString(code, annotations) + ", was: " + obj);
        }
        
        Span span = (Span)obj;
        
        if (code != span.getServiceType()) {
            throw new AssertionError("Expected a Span with serviceType[" + code + "] but was [" + span.getServiceType() + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        List<TAnnotation> actualAnnotations = span.getAnnotations();
        
        try {
            verifyAnnotations(actualAnnotations, annotations);
        } catch (AssertionError e) {
            throw new AssertionError("expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
    }

    private void verifyAnnotations(List<TAnnotation> actualAnnotations, ExpectedAnnotation... annotations) throws AssertionError {
        int len = annotations.length;
        int actualLen = actualAnnotations == null ? 0 : actualAnnotations.size();
        
        if (actualLen != len) {
            throw new AssertionError("Expected [" + len + "] annotations but was [" + actualLen + "]");
        }
        
        for (int i = 0; i < len; i++) {
            ExpectedAnnotation expect = annotations[i];
            TAnnotation actual = actualAnnotations.get(i);
            
            if (expect.getKey() != actual.getKey() || !Objects.equal(expect.getValue(), actual.getValue().getFieldValue())) {
                throw new AssertionError("Expected " + i + "th annotation [" + expect + "] but was [" + toString(actual) + "]");
            }
        }
    }
    
    @Override
    public void verifySpanEvent(ServiceType serviceType, ExpectedAnnotation... annotations) {
        verifySpanEvent(serviceType, null, annotations);
    }
    
    public void verifySpanEvent(ServiceType serviceType, Integer apiId, ExpectedAnnotation... annotations) {
        Object obj = getPeekableSpanDataSender().poll();
        short code = serviceType.getCode();
        
        if (obj == null) {
            throw new AssertionError("No SpanEvent. expected: " + toString(code, annotations));
        }
        
        
        if (!(obj instanceof SpanEvent)) {
            throw new AssertionError("Expected an instance of SpanEvent but was " + obj.getClass().getName() +". expected: " + toString(code, annotations) + ", was: " + obj);
        }
        
        SpanEvent span = (SpanEvent)obj;
        
        if (code != span.getServiceType()) {
            throw new AssertionError("Expected a SpanEvent with serviceType[" + code + "] but was [" + span.getServiceType() + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        if (apiId != null && span.getApiId() != apiId) {
            throw new AssertionError("Expected a SpanEvent with ApiId[" + apiId + "] but was [" + span.getApiId() + "]. expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
        
        List<TAnnotation> actualAnnotations = span.getAnnotations();
        
        try {
            verifyAnnotations(actualAnnotations, annotations);
        } catch (AssertionError e) {
            throw new AssertionError("expected: " + toString(code, annotations) + ", was: " + toString(span));
        }
    }
    
    @Override
    public void verifyApi(ServiceType serviceType, Method method, Object... params) {
        Class<?> clazz = method.getDeclaringClass();
        InstrumentClass ic;
        try {
            ic = getByteCodeInstrumentor().getClass(clazz.getClassLoader(), clazz.getName(), null);
        } catch (InstrumentException e) {
            throw new RuntimeException("Cannot get instruemntClass " + clazz.getName(), e);
        }
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        
        MethodInfo methodInfo = ic.getDeclaredMethod(method.getName(), parameterTypeNames);
        String desc = methodInfo.getDescriptor().getApiDescriptor();
        
        int apiId = ((TestTcpDataSender)getTcpDataSender()).getApiId(desc);

        ExpectedAnnotation[] annotations = new ExpectedAnnotation[params.length];

        for (int i = 0; i < params.length; i++) {
            annotations[i] = ExpectedAnnotation.annotation(AnnotationKey.getArgs(i), params[i]);
        }
        
        verifySpanEvent(serviceType, apiId, annotations);
    }
    
    @Override
    public void printSpans(PrintStream out) {
        for (Object obj : getPeekableSpanDataSender()) {
            out.println(obj);
        }
    }
    
    @Override
    public void printApis(PrintStream out) {
        ((TestTcpDataSender)getTcpDataSender()).printApis(out);
    }

    @Override
    public void initialize(boolean createTraceObject) {
        if (createTraceObject) {
            getTraceContext().newTraceObject();
        }
        
        getPeekableSpanDataSender().clear();
        ((TestTcpDataSender)getTcpDataSender()).clear();
    }

    @Override
    public void cleanUp(boolean detachTraceObject) {
        if (detachTraceObject) {
            getTraceContext().detachTraceObject();
        }
        
        getPeekableSpanDataSender().clear();
        ((TestTcpDataSender)getTcpDataSender()).clear();
    }
    
    
}
