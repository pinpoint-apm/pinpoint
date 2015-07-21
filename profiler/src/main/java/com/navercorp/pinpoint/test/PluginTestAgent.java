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
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.thrift.TBase;

import com.google.common.base.Objects;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedSql;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.bootstrap.plugin.test.TraceType;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.interceptor.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class PluginTestAgent extends DefaultAgent implements PluginTestVerifier {
    
    private TestableServerMetaDataListener serverMetaDataListener;
    private AnnotationKeyRegistryService annotationKeyRegistryService;
    
    private final List<Short> ignoredServiceTypes = new ArrayList<Short>();

    public PluginTestAgent(AgentOption agentOption) {
        super(agentOption, new DefaultInterceptorRegistryBinder());
        this.annotationKeyRegistryService = agentOption.getAnnotationKeyRegistryService();
        PluginTestVerifierHolder.setInstance(this);
    }

    @Override
    protected DataSender createUdpStatDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new ListenableDataSender<TBase<?, ?>>();
    }

    @Override
    protected DataSender createUdpSpanDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        ListenableDataSender<TBase<?, ?>> sender = new ListenableDataSender<TBase<?, ?>>();
        sender.setListener(new OrderedSpanRecorder());
        return sender;
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
    public void verifyServerType(String serviceTypeName) {
        ServiceType expectedType = findServiceType(serviceTypeName);
        ServiceType actualType = getAgentInformation().getServerType();
        
        if (!expectedType.equals(actualType)) {
            throw new AssertionError("ResolvedExpectedTrace server type: " + expectedType.getName() + "[" + expectedType.getCode() + "] but was " + actualType + "[" + actualType.getCode() + "]");
        }
    }
    
    @Override
    public void verifyServerInfo(String expected) {
        String actualName = this.serverMetaDataListener.getServerMetaData().getServerInfo();
        
        if (!actualName.equals(expected)) {
            throw new AssertionError("ResolvedExpectedTrace server name [" + expected + "] but was [" + actualName + "]");
        }
    }

    @Override
    public void verifyConnector(String protocol, int port) {
        Map<Integer, String> connectorMap = this.serverMetaDataListener.getServerMetaData().getConnectors();
        String actualProtocol = connectorMap.get(port);
        
        if (actualProtocol == null || !actualProtocol.equals(protocol)) {
            throw new AssertionError("ResolvedExpectedTrace protocol [" + protocol + "] at port [" + port + "] but was [" + actualProtocol + "]");
        }
    }

    @Override
    public void verifyService(String name, List<String> libs) {
        List<ServiceInfo> serviceInfos = this.serverMetaDataListener.getServerMetaData().getServiceInfos();
        
        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.getServiceName().equals(name)) {
                List<String> actualLibs = serviceInfo.getServiceLibs();
                
                if (actualLibs.size() != libs.size()) {
                    throw new AssertionError("ResolvedExpectedTrace service [" + name + "] with libraries [" + libs + "] but was [" + actualLibs + "]");
                }
                
                for (String lib : libs) {
                    if (!actualLibs.contains(lib)) {
                        throw new AssertionError("ResolvedExpectedTrace service [" + name + "] with libraries [" + libs + "] but was [" + actualLibs + "]");
                    }
                }
                
                // OK
                return;
            }
        }
        
        throw new AssertionError("ResolvedExpectedTrace service [" + name + "] with libraries [" + libs + "] but there is no such service");
    }
    
    private boolean isIgnored(Object obj) {
        short serviceType = -1;
        
        if (obj instanceof TSpan) {
            serviceType = ((TSpan) obj).getServiceType();
        } else if (obj instanceof TSpanEvent) {
            serviceType = ((TSpanEvent) obj).getServiceType();
        }
        
        return ignoredServiceTypes.contains(serviceType);
    }

    @Override
    public void verifyTraceCount(int expected) {
        int actual = 0;
        
        for (Object obj : getRecorder()) {
            if (!isIgnored(obj)) {
                actual++;
            }
        }
        
        if (expected != actual) {
            throw new AssertionError("ResolvedExpectedTrace count: " + expected + ", actual: " + actual);
        }
    }
    
    private ServiceType findServiceType(String name) {
        ServiceType serviceType = getServiceTypeRegistryService().findServiceTypeByName(name);
        
        if (serviceType == ServiceType.UNDEFINED) {
            throw new AssertionError("No such service type: " + name);
        }
        
        return serviceType;
    }
    
    private Class<?> resolveSpanClass(TraceType type) {
        switch (type) {
        case ROOT:
            return Span.class;
        case EVENT:
            return SpanEvent.class;
        }
        
        throw new IllegalArgumentException(type.toString());
    }
    
    @Override
    public void verifyDiscreteTraceBlock(ExpectedTrace... expectations) {
        verifyDiscreteTraceBlock(expectations, null);
    }
        
    public void verifyDiscreteTraceBlock(ExpectedTrace[] expectations, Integer asyncId) {   
        if (expectations == null || expectations.length == 0) {
            throw new IllegalArgumentException("No expectations");
        }
        
        ExpectedTrace expected = expectations[0];
        ResolvedExpectedTrace resolved = resolveExpectedTrace(expected, asyncId);
        
        int i = 0;
        Iterator<?> iterator = getRecorder().iterator();
        
        while (iterator.hasNext()) {
            ActualTrace actual = wrap(iterator.next());

            try {
                verifySpan(resolved, actual);
            } catch (AssertionError e) {
                continue;
            }
            
            iterator.remove();
            verifyAsyncTraces(expected, actual);
            
            if (++i == expectations.length) {
                return;
            }
            
            expected = expectations[i];
            resolved = resolveExpectedTrace(expected, asyncId);
        }
        
        throw new AssertionError("Failed to match " + i + "th expectation: " + resolved);
    }

    @Override
    public void verifyTrace(ExpectedTrace... expectations) {
        if (expectations == null || expectations.length == 0) {
            throw new IllegalArgumentException("No expectations");
        }

        for (ExpectedTrace expected : expectations) {
            ResolvedExpectedTrace resolved = resolveExpectedTrace(expected, null);

            Object actual = popSpan();
            
            if (actual == null) {
                throw new AssertionError("Expected a " + resolved.type.getSimpleName() + " but there is no trace");
            }

            ActualTrace wrapped = wrap(actual);
            
            verifySpan(resolved, wrapped);
            verifyAsyncTraces(expected, wrapped);
        }
    }

    private void verifyAsyncTraces(ExpectedTrace expected, ActualTrace wrapped) throws AssertionError {
        ExpectedTrace[] asyncTraces = expected.getAsyncTraces();
        
        if (asyncTraces != null && asyncTraces.length > 0) {
            Integer asyncId = wrapped.getNextAsyncId();
            
            if (asyncId == null) {
                throw new AssertionError("Expected async traces triggered but nextAsyncId is not present: " + wrapped);
            }
            
            verifyDiscreteTraceBlock(asyncTraces, asyncId);
        }
    }
    
    private ResolvedExpectedTrace resolveExpectedTrace(ExpectedTrace expected, Integer asyncId) throws AssertionError {
        ServiceType serviceType = findServiceType(expected.getServiceType());
        Class<?> spanClass = resolveSpanClass(expected.getType());
        int apiId = expected.getMethod() != null ? findApiId(expected.getMethod()) : (expected.getMethodSignature() == null ? null : findApiId(expected.getMethodSignature()));
        
        return new ResolvedExpectedTrace(spanClass, serviceType, apiId, expected.getRpc(), expected.getEndPoint(), expected.getRemoteAddr(), expected.getDestinationId(), expected.getAnnotations(), asyncId);
    }
    

    @Override
    public void ignoreServiceType(String... serviceTypes) {
        for (String serviceType : serviceTypes) {
            ServiceType t = findServiceType(serviceType);
            ignoredServiceTypes.add(t.getCode());
        }
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

    private interface ActualTrace {
        public Short getServiceType();
        public Integer getApiId();
        public Integer getAsyncId();
        public Integer getNextAsyncId();
        public String getRpc();
        public String getEndPoint();
        public String getRemoteAddr();
        public String getDestinationId();
        public List<TAnnotation> getAnnotations();
        
        public Class<?> getType();
    }
    
    private final class SpanFacade implements ActualTrace {
        private final Span span;
        
        public SpanFacade(Span span) {
            this.span = span;
        }

        @Override
        public Short getServiceType() {
            return span.isSetServiceType() ? span.getServiceType() : null;
        }

        @Override
        public Integer getApiId() {
            return span.isSetApiId() ? span.getApiId() : null;
        }
        
        @Override
        public Integer getAsyncId() {
            return null;
        }
        
        @Override
        public Integer getNextAsyncId() {
            return null;
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
            builder.append(getServiceType());
            builder.append(", apiId: ");
            builder.append(getApiId());
            builder.append(", rpc: ");
            builder.append(getRpc());
            builder.append(", endPoint: ");
            builder.append(getEndPoint());
            builder.append(", remoteAddr: ");
            builder.append(getRemoteAddr());
            builder.append(", [");
            appendAnnotations(builder, getAnnotations());
            builder.append("])");
            
            return builder.toString();
        }
    }

    private final class SpanEventFacade implements ActualTrace {
        private final SpanEvent span;
        
        public SpanEventFacade(SpanEvent span) {
            this.span = span;
        }

        @Override
        public Short getServiceType() {
            return span.isSetServiceType() ? span.getServiceType() : null;
        }

        @Override
        public Integer getApiId() {
            return span.isSetApiId() ? span.getApiId() : null;
        }
        
        @Override
        public Integer getAsyncId() {
            return span.isSetAsyncId() ? span.getAsyncId() : null;
        }

        @Override
        public Integer getNextAsyncId() {
            return span.isSetNextAsyncId() ? span.getNextAsyncId() : null;
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
            builder.append(getServiceType());
            builder.append(", apiId: ");
            builder.append(getApiId());
            builder.append(", rpc: ");
            builder.append(getRpc());
            builder.append(", endPoint: ");
            builder.append(getEndPoint());
            builder.append(", destinationId: ");
            builder.append(getDestinationId());
            builder.append(", [");
            appendAnnotations(builder, getAnnotations());
            builder.append("], asyncId: ");
            builder.append(getAsyncId());
            builder.append("nextAsyncId: ");
            builder.append(getNextAsyncId());
            builder.append(')');
            
            return builder.toString();
        }
    }
    
    private final class ResolvedExpectedTrace {
        private final Class<?> type;
        private final ServiceType serviceType;
        private final Integer asyncId;
        private final Integer apiId;
        private final String rpc;
        private final String endPoint;
        private final String remoteAddr;
        private final String destinationId;
        private final ExpectedAnnotation[] annotations;
        
        public ResolvedExpectedTrace(Class<?> type, ServiceType serviceType, Integer apiId, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation[] annotations, Integer asyncId) {
            this.type = type;
            this.serviceType = serviceType;
            this.apiId = apiId;
            this.rpc = rpc;
            this.endPoint = endPoint;
            this.remoteAddr = remoteAddr;
            this.destinationId = destinationId;
            this.annotations = annotations;
            this.asyncId = asyncId;
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
            builder.append(", asyncId: ");
            builder.append(asyncId);
            builder.append(")");
            
            return builder.toString();
        }
    }
    
    private ActualTrace wrap(Object obj) {
        if (obj instanceof Span) {
            return new SpanFacade((Span)obj);
        } else if (obj instanceof SpanEvent) {
            return new SpanEventFacade((SpanEvent)obj);
        }
        
        throw new IllegalArgumentException("Unexpected type: " + obj.getClass());
    }
    
    private static boolean equals(Object expected, Object actual) {
        // if expected is null, no need to compare.
        return expected == null ? true : (expected.equals(actual));
    }

    private void verifySpan(ResolvedExpectedTrace expected, ActualTrace actual) {
        if (!expected.type.equals(actual.getType())) {
            throw new AssertionError("Expected an instance of " + expected.type.getSimpleName() + " but was " + actual.getType().getName() +". expected: " + expected + ", was: " + actual);
        }
        
        if (expected.serviceType.getCode() != actual.getServiceType()) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with serviceType[" + expected.serviceType.getCode() + "] but was [" + actual.getServiceType() + "]. expected: " + expected + ", was: " + actual);
        }
        
        if (!equals(expected.apiId, actual.getApiId())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with apiId[" + expected.apiId + "] but was [" + actual.getApiId() + "]. expected: " + expected + ", was: " + actual);
        }
        
        if (!equals(expected.rpc, actual.getRpc())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with rpc[" + expected.rpc + "] but was [" + actual.getRpc() + "]. expected: " + expected + ", was: " + actual);
        }

        if (!equals(expected.endPoint, actual.getEndPoint())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with endPoint[" + expected.endPoint + "] but was [" + actual.getEndPoint() + "]. expected: " + expected + ", was: " + actual);
        }
        
        if (!equals(expected.remoteAddr, actual.getRemoteAddr())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with remoteAddr[" + expected.remoteAddr + "] but was [" + actual.getRemoteAddr() + "]. expected: " + expected + ", was: " + actual);
        }
        
        if (!equals(expected.destinationId, actual.getDestinationId())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with destinationId[" + expected.destinationId + "] but was [" + actual.getDestinationId() + "]. expected: " + expected + ", was: " + actual);
        }
        
        if (!equals(expected.asyncId, actual.getAsyncId())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with asyncId[" + expected.asyncId + "] but was [" + actual.getAsyncId() + "]. expected: " + expected + ", was: " + actual);
        }

        
        List<TAnnotation> actualAnnotations = actual.getAnnotations();
        
        int len = expected.annotations == null ? 0 : expected.annotations.length;
        int actualLen = actualAnnotations == null ? 0 : actualAnnotations.size();
        
        if (actualLen != len) {
            throw new AssertionError("Expected [" + len + "] annotations but was [" + actualLen + "], expected: " + expected + ", was: " + actual);
        }
        
        for (int i = 0; i < len; i++) {
            ExpectedAnnotation expect = expected.annotations[i];
            AnnotationKey expectedAnnotationKey = annotationKeyRegistryService.findAnnotationKeyByName(expect.getKeyName());
            TAnnotation actualAnnotation = actualAnnotations.get(i);

            if (expectedAnnotationKey.getCode() != actualAnnotation.getKey()) {
                throw new AssertionError("Expected " + i + "th annotation [" + expectedAnnotationKey.getCode() + "=" + expect.getValue() + "] but was [" + toString(actualAnnotation) + "], expected: " + expected + ", was: " + actual);
            }

            if (expectedAnnotationKey == AnnotationKey.SQL_ID && expect instanceof ExpectedSql) {
                verifySql((ExpectedSql)expect, actualAnnotation);
            } else {
                Object expectedValue = expect.getValue();
                
                if (AnnotationKey.isCachedArgsKey(expectedAnnotationKey.getCode())) {
                    expectedValue = getTestTcpDataSender().getStringId(expectedValue.toString());
                }
                
                if (!Objects.equal(expectedValue, actualAnnotation.getValue().getFieldValue())) {
                    throw new AssertionError("Expected " + i + "th annotation [" + expectedAnnotationKey.getCode() + "=" + expect.getValue() + "] but was [" + toString(actualAnnotation) + "], expected: " + expected + ", was: " + actual);
                }
            }
        }
    }
    
    private void verifySql(ExpectedSql expected, TAnnotation actual) {
        int id = getTestTcpDataSender().getSqlId(expected.getQuery());
        TIntStringStringValue value = actual.getValue().getIntStringStringValue();

        if (value.getIntValue() != id) {
            String actualQuery = getTestTcpDataSender().getSql(value.getIntValue());
            throw new AssertionError("Expected sql [" + id + ": " + expected.getQuery() + "] but was [" + value.getIntValue() + ": " + actualQuery + "], expected: " + expected + ", was: " + actual);
        }
        
        if (!Objects.equal(value.getStringValue1(), expected.getOutput())) {
            throw new AssertionError("Expected sql with output [" + expected.getOutput() + "] but was [" + value.getStringValue1() + "], expected: " + expected + ", was: " + actual);
        }
        
        if (!Objects.equal(value.getStringValue2(), expected.getBindValuesAsString())) {
            throw new AssertionError("Expected sql with bindValues [" + expected.getBindValuesAsString() + "] but was [" + value.getStringValue2() + "], expected: " + expected + ", was: " + actual);
        }
    }
        
    private int findApiId(Member method) throws AssertionError {
        Class<?> clazz = method.getDeclaringClass();
        
        InstrumentClass ic;
        try {
            ic = getByteCodeInstrumentor().getClass(clazz.getClassLoader(), clazz.getName(), null);
        } catch (InstrumentException e) {
            throw new RuntimeException("Cannot get instrumentClass " + clazz.getName(), e);
        }

        MethodInfo methodInfo;
        
        if (method instanceof Method) {
            methodInfo = getMethodInfo(ic, (Method)method);
        } else if (method instanceof Constructor) {
            methodInfo = getMethodInfo(ic, (Constructor<?>)method);
        } else {
            throw new IllegalArgumentException("method: " + method);
        }
        
        String desc = methodInfo.getDescriptor().getFullName();
        
        return findApiId(desc);
    }
    
    private MethodInfo getMethodInfo(InstrumentClass ic, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterTypeNames = JavaAssistUtils.toPinpointParameterType(parameterTypes);
        
        return ic.getDeclaredMethod(method.getName(), parameterTypeNames);
    }
    
    private MethodInfo getMethodInfo(InstrumentClass ic, Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        String[] parameterTypeNames = JavaAssistUtils.getParameterType(parameterTypes);
        
        return ic.getConstructor(parameterTypeNames);
    }


    private int findApiId(String desc) throws AssertionError {
        try {
            return getTestTcpDataSender().getApiId(desc);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Cannot find apiId of [" + desc + "]");
        }
    }

    private TestTcpDataSender getTestTcpDataSender() {
        return (TestTcpDataSender)getTcpDataSender();
    }
    
    private OrderedSpanRecorder getRecorder() {
        return (OrderedSpanRecorder)((ListenableDataSender<?>)getSpanDataSender()).getListener();
    }
    
    private Object popSpan() {
        while (true) {
            Object obj = getRecorder().pop();
            
            if (obj == null) {
                return null;
            }
            
            if (!isIgnored(obj)) {
                return obj;
            }
        }
    }

    @Override
    public void printCache(PrintStream out) {
        getRecorder().print(out);
        getTestTcpDataSender().printDatas(out);
    }
    
    @Override
    public void printCache() {
        printCache(System.out);
    }

    @Override
    public void initialize(boolean createTraceObject) {
        if (createTraceObject) {
            Trace trace = getTraceContext().newTraceObject();
        }
        
        getRecorder().clear();
        getTestTcpDataSender().clear();
        ignoredServiceTypes.clear();
    }

    @Override
    public void cleanUp(boolean detachTraceObject) {
        if (detachTraceObject) {
            getTraceContext().removeTraceObject();
        }

        getRecorder().clear();
        getTestTcpDataSender().clear();
        ignoredServiceTypes.clear();
    }

    @Override
    public void verifyIsLoggingTransactionInfo(LoggingInfo loggingInfo) {
        Object actual = popSpan();
        Span span = null;

        if (actual instanceof Span) {
            span = (Span)actual;
        } else if (actual instanceof SpanEvent) {
            span = ((SpanEvent)actual).getSpan();
        } else {
            throw new IllegalArgumentException("Unexpected type: " + actual.getClass());
        }
        
        if (span.getLoggingTransactionInfo() != loggingInfo.getCode()) {
            
            LoggingInfo loggingTransactionInfo = LoggingInfo.searchByCode(span.getLoggingTransactionInfo());
            
            if (loggingTransactionInfo != null) {
                throw new AssertionError("Expected a Span isLoggingTransactionInfo value with [" + loggingInfo.getName() + "] but was [" + loggingTransactionInfo.getName() + "]. expected: " + loggingInfo.getName() + ", was: " + loggingTransactionInfo.getName());
            } else {
                throw new AssertionError("Expected a Span isLoggingTransactionInfo value with [" + loggingInfo.getName() + "] but loggingTransactionInfo value invalid.");
            }
            
        }
        
        
    }

}
