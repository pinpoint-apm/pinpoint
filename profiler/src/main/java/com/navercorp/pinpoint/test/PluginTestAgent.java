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

import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.thrift.TBase;

import com.google.common.base.Objects;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class PluginTestAgent extends DefaultAgent implements PluginTestVerifier {
    
    private TestableServerMetaDataListener serverMetaDataListener;
    
    public PluginTestAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig, URL[] pluginJars) {
        super(agentArgs, instrumentation, profilerConfig, pluginJars);
        PluginTestVerifierHolder.setInstance(this);
    }

    @Override
    protected DataSender createUdpStatDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new ListenableDataSender<TBase<?, ?>>();
    }

    @Override
    protected DataSender createUdpSpanDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new ListenableDataSender<TBase<?, ?>>();
    }

    public DataSender getSpanDataSender() {
        return super.getSpanDataSender();
    }


    @Override
    protected StorageFactory createStorageFactory() {
        return new SimpleSpanStorageFactory(super.getSpanDataSender());
    }


    @Override
    protected EnhancedDataSender createTcpDataSender(CommandDispatcher commandDispatcher) {
        return new TestTcpDataSender();
    }

    @Override
    protected ServerMetaDataHolder createServerMetaDataHolder() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        ServerMetaDataHolder serverMetaDataHolder = new ResettableServerMetaDataHolder(vmArgs);
        this.serverMetaDataListener = new TestableServerMetaDataListener();
        serverMetaDataHolder.addListener(this.serverMetaDataListener);
        return serverMetaDataHolder;
    }
    
    @Override
    public void verifyServerType(ServiceType serviceType) {
        ServiceType actualType = getAgentInformation().getServerType();
        
        if (serviceType != actualType) {
            throw new AssertionError("Expected server type: " + serviceType.getName() + "[" + serviceType.getCode() + "] but was " + actualType + "[" + actualType.getCode() + "]");
        }
    }
    
    @Override
    public void verifyServerInfo(String expected) {
        String actualName = this.serverMetaDataListener.getServerMetaData().getServerInfo();
        
        if (!actualName.equals(expected)) {
            throw new AssertionError("Expected server name [" + expected + "] but was [" + actualName + "]");
        }
    }

    @Override
    public void verifyConnector(String protocol, int port) {
        Map<Integer, String> connectorMap = this.serverMetaDataListener.getServerMetaData().getConnectors();
        String actualProtocol = connectorMap.get(port);
        
        if (actualProtocol == null || !actualProtocol.equals(protocol)) {
            throw new AssertionError("Expected protocol [" + protocol + "] at port [" + port + "] but was [" + actualProtocol + "]");
        }
    }

    @Override
    public void verifyService(String name, List<String> libs) {
        List<ServiceInfo> serviceInfos = this.serverMetaDataListener.getServerMetaData().getServiceInfos();
        
        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.getServiceName().equals(name)) {
                List<String> actualLibs = serviceInfo.getServiceLibs();
                
                if (actualLibs.equals(libs)) {
                    return;
                } else {
                    throw new AssertionError("Expected service [" + name + "] with libraries [" + libs + "] but was [" + actualLibs + "]");
                }
            }
        }
        
        throw new AssertionError("Expected service [" + name + "] with libraries [" + libs + "] but there is no such service");
    }

    @Override
    public void verifySpanCount(int expected) {
        int actual = getTBaseRecorder().size();
        
        if (expected != actual) {
            throw new AssertionError("Expected count: " + expected + ", actual: " + actual);
        }
    }
    
    @Override
    public void verifySpan(ServiceType serviceType, ExpectedAnnotation... annotations) {
        Expected expected = new Expected(Span.class, serviceType, null, null, null, null, null, annotations);
        verifySpan(expected);
    }
    
    @Override
    public void verifySpanEvent(ServiceType serviceType, ExpectedAnnotation... annotations) {
        Expected expected = new Expected(SpanEvent.class, serviceType, null, null, null, null, null, annotations);
        verifySpan(expected);
    }

    @Override
    public void verifySpan(ServiceType serviceType, Method method, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        int apiId = findApiId(method);
        Expected expected = new Expected(Span.class, serviceType, apiId, rpc, endPoint, remoteAddr, null, annotations);
        verifySpan(expected);
    }
    
    @Override
    public void verifySpanEvent(ServiceType serviceType, Method method, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        int apiId = findApiId(method);
        Expected expected = new Expected(Span.class, serviceType, apiId, rpc, endPoint, null, destinationId, annotations);
        verifySpan(expected);
    }
    
    @Override
    public void popSpan() {
        getTBaseRecorder().poll();
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

    private interface Facade {
        public short getServiceType();
        public int getApiId();
        public String getRpc();
        public String getEndPoint();
        public String getRemoteAddr();
        public String getDestinationId();
        public List<TAnnotation> getAnnotations();
        
        public Class<?> getType();
    }
    
    private final class SpanFacade implements Facade {
        private final Span span;
        
        public SpanFacade(Span span) {
            this.span = span;
        }

        @Override
        public short getServiceType() {
            return span.getServiceType();
        }

        @Override
        public int getApiId() {
            return span.getApiId();
        }

        @Override
        public String getRpc() {
            return span.getRpc();
        }

        @Override
        public String getEndPoint() {
            return span.getEndPoint();
        }

        @Override
        public String getRemoteAddr() {
            return span.getRemoteAddr();
        }

        @Override
        public String getDestinationId() {
            return null;
        }

        @Override
        public List<TAnnotation> getAnnotations() {
            return span.getAnnotations();
        }

        @Override
        public Class<?> getType() {
            return Span.class;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(serviceType: ");
            builder.append(span.getServiceType());
            builder.append(", apiId: ");
            builder.append(span.getApiId());
            builder.append(", rpc: ");
            builder.append(span.getRpc());
            builder.append(", endPoint: ");
            builder.append(span.getEndPoint());
            builder.append(", remoteAddr: ");
            builder.append(span.getRemoteAddr());
            builder.append(", [");
            appendAnnotations(builder, span.getAnnotations());
            builder.append("])");
            
            return builder.toString();
        }
    }

    private final class SpanEventFacade implements Facade {
        private final SpanEvent span;
        
        public SpanEventFacade(SpanEvent span) {
            this.span = span;
        }

        @Override
        public short getServiceType() {
            return span.getServiceType();
        }

        @Override
        public int getApiId() {
            return span.getApiId();
        }

        @Override
        public String getRpc() {
            return span.getRpc();
        }

        @Override
        public String getEndPoint() {
            return span.getEndPoint();
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public String getDestinationId() {
            return span.getDestinationId();
        }

        @Override
        public List<TAnnotation> getAnnotations() {
            return span.getAnnotations();
        }

        @Override
        public Class<?> getType() {
            return SpanEvent.class;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(serviceType: ");
            builder.append(span.getServiceType());
            builder.append(", apiId: ");
            builder.append(span.getApiId());
            builder.append(", rpc: ");
            builder.append(span.getRpc());
            builder.append(", endPoint: ");
            builder.append(span.getEndPoint());
            builder.append(", destinationId: ");
            builder.append(span.getDestinationId());
            builder.append(", [");
            appendAnnotations(builder, span.getAnnotations());
            builder.append("])");
            
            return builder.toString();
        }
    }
    
    private final class Expected {
        private final Class<?> type;
        private final ServiceType serviceType;
        private final Integer apiId;
        private final String rpc;
        private final String endPoint;
        private final String remoteAddr;
        private final String destinationId;
        private final ExpectedAnnotation[] annotations;
        
        public Expected(Class<?> type, ServiceType serviceType, Integer apiId, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation[] annotations) {
            this.type = type;
            this.serviceType = serviceType;
            this.apiId = apiId;
            this.rpc = rpc;
            this.endPoint = endPoint;
            this.remoteAddr = remoteAddr;
            this.destinationId = destinationId;
            this.annotations = annotations;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            
            builder.append(type.getSimpleName());
            builder.append("(serviceType: ");
            builder.append(serviceType.getCode());
            builder.append(", apiId: ");
            builder.append(apiId);
            builder.append(", rpc: ");
            builder.append(rpc);
            builder.append(", endPoint: ");
            builder.append(endPoint);
            builder.append(", remoteAddr: ");
            builder.append(remoteAddr);
            builder.append(", destinationId: ");
            builder.append(destinationId);
            builder.append(", annotations: ");
            builder.append(Arrays.deepToString(annotations));
            builder.append(")");
            
            return builder.toString();
        }
    }
    
    private Facade wrap(Object obj) {
        if (obj instanceof Span) {
            return new SpanFacade((Span)obj);
        } else if (obj instanceof SpanEvent) {
            return new SpanEventFacade((SpanEvent)obj);
        }
        
        throw new IllegalArgumentException("Unexpected type: " + obj.getClass());
    }
    
    private static boolean equals(Object o1, Object o2) {
        // TODO to make tests more reliable below is better. but it makes tests inconvenience.
//        return o1 == null ? (o2 == null) : (o1.equals(o2));
        
        return o1 == null ? true : (o1.equals(o2));
    }

    private void verifySpan(Expected expected) {
        Object obj = getTBaseRecorder().poll();
        
        if (obj == null) {
            throw new AssertionError("No " + expected.type.getSimpleName() + ". expected: " + expected);
        }
        
        if (!expected.type.isInstance(obj)) {
            throw new AssertionError("Expected an instance of " + expected.type.getSimpleName() + " but was " + obj.getClass().getName() +". expected: " + expected + ", was: " + obj);
        }
        
        Facade span = wrap(obj);
        
        if (expected.serviceType.getCode() != span.getServiceType()) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with serviceType[" + expected.serviceType.getCode() + "] but was [" + span.getServiceType() + "]. expected: " + expected + ", was: " + span);
        }
        
        if (!equals(expected.apiId, span.getApiId())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with apiId[" + expected.apiId + "] but was [" + span.getApiId() + "]. expected: " + expected + ", was: " + span);
        }
        
        if (!equals(expected.rpc, span.getRpc())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with rpc[" + expected.rpc + "] but was [" + span.getRpc() + "]. expected: " + expected + ", was: " + span);
        }

        if (!equals(expected.endPoint, span.getEndPoint())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with endPoint[" + expected.endPoint + "] but was [" + span.getEndPoint() + "]. expected: " + expected + ", was: " + span);
        }
        
        if (!equals(expected.remoteAddr, span.getRemoteAddr())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with remoteAddr[" + expected.remoteAddr + "] but was [" + span.getRemoteAddr() + "]. expected: " + expected + ", was: " + span);
        }
        
        if (!equals(expected.destinationId, span.getDestinationId())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with destinationId[" + expected.destinationId + "] but was [" + span.getDestinationId() + "]. expected: " + expected + ", was: " + span);
        }
        
        List<TAnnotation> actualAnnotations = span.getAnnotations();
        
        int len = expected.annotations.length;
        int actualLen = actualAnnotations == null ? 0 : actualAnnotations.size();
        
        if (actualLen != len) {
            throw new AssertionError("Expected [" + len + "] annotations but was [" + actualLen + "], expected: " + expected + ", was: " + span);
        }
        
        for (int i = 0; i < len; i++) {
            ExpectedAnnotation expect = expected.annotations[i];
            TAnnotation actual = actualAnnotations.get(i);
            
            if (expect.getKey() != actual.getKey() || !Objects.equal(expect.getValue(), actual.getValue().getFieldValue())) {
                throw new AssertionError("Expected " + i + "th annotation [" + expect + "] but was [" + toString(actual) + "], expected: " + expected + ", was: " + span);
            }
        }
    }
    
    @Override
    public void verifyApi(ServiceType serviceType, Method method, Object... args) {
        int apiId = findApiId(method);
        ExpectedAnnotation[] annotations = ExpectedAnnotation.args(args);
        Expected expected = new Expected(SpanEvent.class, serviceType, apiId, null, null, null, null, annotations);
        verifySpan(expected);
    }
    
    private int findApiId(Method method) throws AssertionError {
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
        String desc = methodInfo.getDescriptor().getFullName();
        
        int apiId;
        
        try {
            apiId = ((TestTcpDataSender)getTcpDataSender()).getApiId(desc);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Cannot find apiId of [" + desc + "]");
        }
        return apiId;
    }
    
    @Override
    public void printSpans(PrintStream out) {
        for (Object obj : getTBaseRecorder()) {
            out.println(obj);
        }
    }

    private TBaseRecorder getTBaseRecorder() {
        DataSender spanDataSender = getSpanDataSender();
        ListenableDataSender listenableDataSender = (ListenableDataSender) spanDataSender;
        TBaseRecorderAdaptor listener = (TBaseRecorderAdaptor) listenableDataSender.getListener();
        return listener.getRecorder();
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
        
        DataSender spanDataSender = getSpanDataSender();
        
        if (spanDataSender instanceof ListenableDataSender) {
            ListenableDataSender listenableDataSender = (ListenableDataSender) spanDataSender;
            listenableDataSender.setListener(new TBaseRecorderAdaptor());
        }
        
        ((TestTcpDataSender)getTcpDataSender()).clear();
    }

    @Override
    public void cleanUp(boolean detachTraceObject) {
        if (detachTraceObject) {
            getTraceContext().detachTraceObject();
        }

        ((TestTcpDataSender)getTcpDataSender()).clear();
    }
}
