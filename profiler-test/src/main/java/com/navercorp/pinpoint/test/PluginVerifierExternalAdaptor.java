/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test;

import com.google.common.base.Objects;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedSql;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTraceField;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.TraceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.StopWatch;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.DefaultLocalAsyncId;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

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

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginVerifierExternalAdaptor implements PluginTestVerifier {

    private final List<Short> ignoredServiceTypes = new ArrayList<Short>();

    private final DefaultApplicationContext applicationContext;

    private final ApplicationContextHandler handler;

    public PluginVerifierExternalAdaptor(DefaultApplicationContext applicationContext) {
        this.applicationContext = Assert.requireNonNull(applicationContext, "applicationContext");
        this.handler = new ApplicationContextHandler(applicationContext);
    }

    public DefaultApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void verifyServerType(String serviceTypeName) {
        final DefaultApplicationContext applicationContext = getApplicationContext();

        ServiceType expectedType = findServiceType(serviceTypeName);
        ServiceType actualType = applicationContext.getAgentInformation().getServerType();

        if (!expectedType.equals(actualType)) {
            throw new AssertionError("ResolvedExpectedTrace server type: " + expectedType.getName() + "[" + expectedType.getCode() + "] but was " + actualType + "[" + actualType.getCode() + "]");
        }
    }

    @Override
    public void verifyServerInfo(String expected) {
        String actualName = getServerMetaData().getServerInfo();

        if (!actualName.equals(expected)) {
            throw new AssertionError("ResolvedExpectedTrace server name [" + expected + "] but was [" + actualName + "]");
        }
    }

    @Override
    public void verifyConnector(String protocol, int port) {
        Map<Integer, String> connectorMap = getServerMetaData().getConnectors();
        String actualProtocol = connectorMap.get(port);

        if (actualProtocol == null || !actualProtocol.equals(protocol)) {
            throw new AssertionError("ResolvedExpectedTrace protocol [" + protocol + "] at port [" + port + "] but was [" + actualProtocol + "]");
        }
    }

    @Override
    public void verifyService(String name, List<String> libs) {
        List<ServiceInfo> serviceInfos = getServerMetaData().getServiceInfos();

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
        final short serviceType = getServiceTypeCode(obj);
        return ignoredServiceTypes.contains(serviceType);
    }

    private short getServiceTypeCode(Object obj) {

        if (obj instanceof Span) {
            final Span span = (Span) obj;
            return span.getServiceType();
        }
        if (obj instanceof SpanChunk) {
            final SpanChunk spanChunk = (SpanChunk) obj;
            List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
            if (spanEventList.size() != 1) {
                throw new IllegalStateException("unexpected spanEventList.size() !=1");
            }
            SpanEvent spanEvent = spanEventList.get(0);
            return spanEvent.getServiceType();
        }
        return -1;
    }

    @Override
    public void verifyTraceCount(int expected) {
        int actual = 0;

        for (Object obj : this.handler.getOrderedSpanRecorder()) {
            if (!isIgnored(obj)) {
                actual++;
            }
        }

        if (expected != actual) {
            throw new AssertionError("ResolvedExpectedTrace count: " + expected + ", actual: " + actual);
        }
    }

    private ServiceType findServiceType(String name) {
        ServiceTypeRegistryService serviceTypeRegistryService = handler.getServiceTypeRegistry();
        ServiceType serviceType = serviceTypeRegistryService.findServiceTypeByName(name);

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
    public void verifyDiscreteTrace(ExpectedTrace... expectations) {
        verifyDiscreteTraceBlock(expectations, null);
    }

    public void verifyDiscreteTraceBlock(ExpectedTrace[] expectations, Integer asyncId) {
        if (ArrayUtils.isEmpty(expectations)) {
            throw new IllegalArgumentException("No expectations");
        }

        ExpectedTrace expected = expectations[0];
        ResolvedExpectedTrace resolved = resolveExpectedTrace(expected, asyncId);

        int i = 0;
        Iterator<?> iterator = this.handler.getOrderedSpanRecorder().iterator();

        while (iterator.hasNext()) {
            final Object next = iterator.next();
            ActualTrace actual = wrap(next);

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
        if (ArrayUtils.isEmpty(expectations)) {
            throw new IllegalArgumentException("No expectations");
        }

        for (ExpectedTrace expected : expectations) {
            ResolvedExpectedTrace resolved = resolveExpectedTrace(expected, null);

            final Item item = popItem();
            if (item == null) {
                throw new AssertionError("Expected a " + resolved.toString() + " but there is no trace");
            }
            final Object actual = item.getValue();

            ActualTrace wrapped = wrap(actual);

            verifySpan(resolved, wrapped);
            verifyAsyncTraces(expected, wrapped);
        }
    }

    private void verifyAsyncTraces(ExpectedTrace expected, ActualTrace wrapped) throws AssertionError {
        final ExpectedTrace[] asyncTraces = expected.getAsyncTraces();

        if (asyncTraces != null && asyncTraces.length > 0) {
            Integer asyncId = wrapped.getNextAsyncId();

            if (asyncId == null) {
                throw new AssertionError("Expected async traces triggered but nextAsyncId is not present: " + wrapped);
            }

            verifyDiscreteTraceBlock(asyncTraces, asyncId);
        }
    }

    private ResolvedExpectedTrace resolveExpectedTrace(ExpectedTrace expected, Integer asyncId) throws AssertionError {
        final ServiceType serviceType = findServiceType(expected.getServiceType());
        final Class<?> spanClass = resolveSpanClass(expected.getType());
        final int apiId = getApiId(expected);

        return new ResolvedExpectedTrace(spanClass, serviceType, apiId, expected.getException(), expected.getRpc(), expected.getEndPoint(), expected.getRemoteAddr(), expected.getDestinationId(), expected.getAnnotations(), asyncId);
    }

    private int getApiId(ExpectedTrace expected) {
        final Member method = expected.getMethod();
        if (method == null) {
            if (expected.getMethodSignature() == null) {
//                return null;
                throw new RuntimeException("Method or MethodSignature is null");
            } else {
                String methodSignature = expected.getMethodSignature();
                if (methodSignature.indexOf('(') != -1) {
                    methodSignature = MethodDescriptionUtils.toJavaMethodDescriptor(methodSignature);
                }
                return findApiId(methodSignature);
            }
        } else {
            return findApiId(method);
        }
    }


    @Override
    public void ignoreServiceType(String... serviceTypes) {
        for (String serviceType : serviceTypes) {
            ServiceType t = findServiceType(serviceType);
            ignoredServiceTypes.add(t.getCode());
        }
    }

    private static void appendAnnotations(StringBuilder builder, List<Annotation> annotations) {
        boolean first = true;

        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }

                builder.append(toString(annotation));
            }
        }
    }

    private static String toString(Annotation a) {
        return a.getAnnotationKey() + "=" + a.getValue();
    }

    private interface ActualTrace {
        Short getServiceType();

        Integer getApiId();

        Integer getAsyncId();

        Integer getNextAsyncId();

        IntStringValue getExceptionInfo();

        String getRpc();

        String getEndPoint();

        String getRemoteAddr();

        String getDestinationId();

        List<Annotation> getAnnotations();

        Class<?> getType();
    }

    private static final class SpanFacade implements ActualTrace {
        private final Span span;

        public SpanFacade(Span span) {
            this.span = span;
        }

        @Override
        public Short getServiceType() {
            final short serviceType = span.getServiceType();
            if (serviceType == 0) {
                return null;
            }
            return serviceType;
        }

        @Override
        public Integer getApiId() {
            final int apiId = span.getApiId();
            if (apiId == 0) {
                return null;
            }
            return apiId;
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
        public IntStringValue getExceptionInfo() {
            return span.getExceptionInfo();
        }

        @Override
        public String getRpc() {
            return span.getTraceRoot().getShared().getRpcName();
        }

        @Override
        public String getEndPoint() {
            return span.getTraceRoot().getShared().getEndPoint();
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
        public List<Annotation> getAnnotations() {
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
            builder.append(", exceptionInfo: ");
            builder.append(getExceptionInfo());
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

    private static final class SpanEventFacade implements ActualTrace {
        private final SpanChunk spanChunk;
        private final SpanEvent spanEvent;

        public SpanEventFacade(SpanChunk spanChunk) {
            this.spanChunk = spanChunk;
            List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
            if (spanEventList.size() != 1) {
                throw new IllegalArgumentException("spanChunk list size == 0");
            }
            this.spanEvent = spanEventList.get(0);
        }

        @Override
        public Short getServiceType() {
            return spanEvent.getServiceType();
        }

        @Override
        public Integer getApiId() {
            return spanEvent.getApiId();
        }

        @Override
        public Integer getAsyncId() {
            if (!(spanChunk instanceof AsyncSpanChunk)) {
                return null;
            }
            AsyncSpanChunk asyncSpanChunk = (AsyncSpanChunk) this.spanChunk;
            LocalAsyncId localAsyncId = asyncSpanChunk.getLocalAsyncId();
            return localAsyncId.getAsyncId();
        }

        @Override
        public Integer getNextAsyncId() {
            return spanEvent.getAsyncIdObject() != null ? spanEvent.getAsyncIdObject().getAsyncId() : null;
        }

        @Override
        public IntStringValue getExceptionInfo() {
            return spanEvent.getExceptionInfo();
        }

        @Override
        public String getRpc() {
            return null;
        }

        @Override
        public String getEndPoint() {
            return spanEvent.getEndPoint();
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public String getDestinationId() {
            return spanEvent.getDestinationId();
        }

        @Override
        public List<Annotation> getAnnotations() {
            return spanEvent.getAnnotations();
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
            builder.append(", exceptionInfo: ");
            builder.append(getExceptionInfo());
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

    private static final class ResolvedExpectedTrace {
        private final Class<?> type;
        private final ServiceType serviceType;
        private final LocalAsyncId localAsyncId;
        private final Integer apiId;
        private final Exception exception;
        private final ExpectedTraceField rpc;
        private final ExpectedTraceField endPoint;
        private final ExpectedTraceField remoteAddr;
        private final ExpectedTraceField destinationId;
        private final ExpectedAnnotation[] annotations;

        public ResolvedExpectedTrace(Class<?> type, ServiceType serviceType, Integer apiId, Exception exception, ExpectedTraceField rpc, ExpectedTraceField endPoint, ExpectedTraceField remoteAddr, ExpectedTraceField destinationId, ExpectedAnnotation[] annotations, Integer asyncId) {
            this.type = type;
            this.serviceType = serviceType;
            this.apiId = apiId;
            this.exception = exception;
            this.rpc = rpc;
            this.endPoint = endPoint;
            this.remoteAddr = remoteAddr;
            this.destinationId = destinationId;
            this.annotations = annotations;
            this.localAsyncId = newLocalAsyncId(asyncId);
        }

        private LocalAsyncId newLocalAsyncId(Integer asyncId) {
            if (asyncId == null) {
                return null;
            }
            return new DefaultLocalAsyncId(asyncId, (short) 0);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append(type.getSimpleName());
            builder.append("(serviceType: ");
            builder.append(serviceType.getCode());
            builder.append(", apiId: ");
            builder.append(apiId);
            builder.append(", exception: ");
            builder.append(exception);
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
            builder.append(", localAsyncId: ");
            builder.append(localAsyncId);
            builder.append(")");

            return builder.toString();
        }
    }

    private ActualTrace wrap(Object obj) {
        if (obj instanceof Span) {
            final Span span = (Span) obj;
            return new SpanFacade(span);
        } else if (obj instanceof SpanChunk) {
            final SpanChunk spanChunk = (SpanChunk) obj;
            return new SpanEventFacade(spanChunk);
        }

        throw new IllegalArgumentException("Unexpected type: " + obj.getClass());
    }

    private static boolean equals(Object expected, Object actual) {
        // if expected is null, no need to compare.
        return expected == null || (expected.equals(actual));
    }

    private static boolean equals(Object expected, String actual) {
        if (expected instanceof ExpectedTraceField) {
            return ((ExpectedTraceField) expected).isEquals(actual);
        }

        // if expected is null, no need to compare.
        return expected == null || (expected.equals(actual));
    }

    private void verifySpan(final ResolvedExpectedTrace expected, ActualTrace actual) {
        if (!expected.type.equals(actual.getType())) {
            throw new AssertionError("Expected an instance of " + expected.type.getSimpleName() + " but was " + actual.getType().getName() + ". expected: " + expected + ", was: " + actual);
        }

        if (!equals(expected.serviceType.getCode(), actual.getServiceType())) {
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

        if (!equals(getAsyncId(expected), actual.getAsyncId())) {
            throw new AssertionError("Expected a " + expected.type.getSimpleName() + " with asyncId[" + expected.localAsyncId + "] but was [" + actual.getAsyncId() + "]. expected: " + expected + ", was: " + actual);
        }

        if (expected.exception != null) {
            final IntStringValue actualExceptionInfo = actual.getExceptionInfo();
            if (actualExceptionInfo != null) {
                String actualExceptionClassName = this.handler.getTcpDataSender().getString(actualExceptionInfo.getIntValue());
                String actualExceptionMessage = actualExceptionInfo.getStringValue();
                verifyException(expected.exception, actualExceptionClassName, actualExceptionMessage);
            } else {
                throw new AssertionError("Expected [" + expected.exception.getClass().getName() + "] but was none");
            }
        }

        List<Annotation> actualAnnotations = actual.getAnnotations();

        int len = expected.annotations == null ? 0 : expected.annotations.length;
        int actualLen = actualAnnotations == null ? 0 : actualAnnotations.size();

        if (actualLen != len) {
            throw new AssertionError("Expected [" + len + "] annotations but was [" + actualLen + "], expected: " + expected + ", was: " + actual);
        }

        for (int i = 0; i < len; i++) {
            ExpectedAnnotation expect = expected.annotations[i];
            AnnotationKey expectedAnnotationKey = this.handler.getAnnotationKeyRegistryService().findAnnotationKeyByName(expect.getKeyName());
            Annotation actualAnnotation = actualAnnotations.get(i);

            if (expectedAnnotationKey.getCode() != actualAnnotation.getAnnotationKey()) {
                throw new AssertionError("Code Different, Expected " + i + "th annotation [" + expectedAnnotationKey.getCode() + "=" + expect.getValue() + "] but was [" + toString(actualAnnotation) + "], expected: " + expected + ", was: " + actual);
            }

            if (expectedAnnotationKey == AnnotationKey.SQL_ID && expect instanceof ExpectedSql) {
                verifySql((ExpectedSql) expect, actualAnnotation);
            } else if (expect.getValue() instanceof StringStringValue){
                verifyStringStringValue(((StringStringValue)expect.getValue()), actualAnnotation);
            } else {
                Object expectedValue = expect.getValue();

                if (expectedValue == Expectations.anyAnnotationValue()) {
                    continue;
                }

                if (AnnotationKeyUtils.isCachedArgsKey(expectedAnnotationKey.getCode())) {
                    expectedValue = this.handler.getTcpDataSender().getStringId(expectedValue.toString());
                }

                if (!Objects.equal(expectedValue, actualAnnotation.getValue())) {

                    throw new AssertionError("Value Different, Expected " + i + "th annotation [" + expectedAnnotationKey.getCode() + "=" + expect.getValue() + "] but was [" + toString(actualAnnotation) + "], expected: " + expected + ", was: " + actual);
                }
            }
        }
    }

    private void verifyStringStringValue(StringStringValue value, Annotation actualAnnotation) {
        StringStringValue annotationValue = (StringStringValue) actualAnnotation.getValue();
        if (!Objects.equal(value.getStringValue1(), annotationValue.getStringValue1())) {
            throw new AssertionError("Expected [" + value.getStringValue1() + "] but was [" + annotationValue.getStringValue1() + "]");
        }

        if (!Objects.equal(value.getStringValue2(), annotationValue.getStringValue2())) {
            throw new AssertionError("Expected [" + value.getStringValue2() + "] but was [" + annotationValue.getStringValue2() + "]");
        }

    }

    private Integer getAsyncId(ResolvedExpectedTrace expected) {
        if (expected.localAsyncId == null) {
            return null;
        }
        return expected.localAsyncId.getAsyncId();
    }

    private void verifyException(Exception expectedException, String actualExceptionClassName, String actualExceptionMessage) {
        String expectedExceptionClassName = expectedException.getClass().getName();
        String expectedExceptionMessage = StringUtils.abbreviate(expectedException.getMessage(), 256);
        if (!Objects.equal(actualExceptionClassName, expectedExceptionClassName)) {
            throw new AssertionError("Expected [" + expectedExceptionClassName + "] but was [" + actualExceptionClassName + "]");
        }
        if (!Objects.equal(actualExceptionMessage, expectedExceptionMessage)) {
            throw new AssertionError("Expected exception with message [" + expectedExceptionMessage + "] but was [" + actualExceptionMessage + "]");
        }
    }

    private void verifySql(ExpectedSql expected, Annotation actual) {
        int id = this.handler.getTcpDataSender().getSqlId(expected.getQuery());
        IntStringStringValue value = (IntStringStringValue) actual.getValue();

        if (value.getIntValue() != id) {
            String actualQuery = this.handler.getTcpDataSender().getSql(value.getIntValue());
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
        final String desc = getMemberInfo(method);
        return findApiId(desc);
    }

    private String getMemberInfo(Member method) {
        if (method instanceof Method) {
            return getMethodInfo((Method) method);
        } else if (method instanceof Constructor) {
            return getConstructorInfo((Constructor<?>) method);
        } else {
            throw new IllegalArgumentException("method: " + method);
        }
    }

    private String getMethodInfo(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterTypeNames = JavaAssistUtils.toPinpointParameterType(parameterTypes);
        return MethodDescriptionUtils.toJavaMethodDescriptor(method.getDeclaringClass().getName(), method.getName(), parameterTypeNames);
    }

    private String getConstructorInfo(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        String[] parameterTypeNames = JavaAssistUtils.toPinpointParameterType(parameterTypes);

        final String constructorSimpleName = MethodDescriptionUtils.getConstructorSimpleName(constructor);
        return MethodDescriptionUtils.toJavaMethodDescriptor(constructor.getDeclaringClass().getName(), constructorSimpleName, parameterTypeNames);
    }

    private int findApiId(String desc) throws AssertionError {
        try {
            return this.handler.getTcpDataSender().getApiId(desc);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Cannot find apiId of [" + desc + "]");
        }
    }



    private ServerMetaData getServerMetaData() {
        return this.handler.getServerMetaDataRegistryService().getServerMetaData();
    }



    private Item popItem() {
        while (true) {
            OrderedSpanRecorder recorder = this.handler.getOrderedSpanRecorder();
            Item item = recorder.popItem();
            if (item == null) {
                return null;
            }

            if (!isIgnored(item.getValue())) {
                return item;
            }
        }
    }

    @Override
    public void printCache(PrintStream out) {
        this.handler.getOrderedSpanRecorder().print(out);
        this.handler.getTcpDataSender().printDatas(out);
    }

    @Override
    public void printCache() {
        printCache(System.out);
    }

    @Override
    public void initialize(boolean createTraceObject) {
        if (createTraceObject) {
            final TraceContext traceContext = getTraceContext();
            traceContext.newTraceObject();
        }

        this.handler.getOrderedSpanRecorder().clear();
        this.handler.getTcpDataSender().clear();
        ignoredServiceTypes.clear();
    }

    @Override
    public void cleanUp(boolean detachTraceObject) {
        if (detachTraceObject) {
            final TraceContext traceContext = getTraceContext();
            traceContext.removeTraceObject();
        }

        this.handler.getOrderedSpanRecorder().clear();
        this.handler.getTcpDataSender().clear();
        ignoredServiceTypes.clear();
    }

    private TraceContext getTraceContext() {
        DefaultApplicationContext applicationContext = getApplicationContext();
        return applicationContext.getTraceContext();
    }

    @Override
    public void verifyIsLoggingTransactionInfo(LoggingInfo loggingInfo) {
        final Item item = popItem();
        if (item == null) {
            throw new AssertionError("Expected a Span isLoggingTransactionInfo value with [" + loggingInfo.getName() + "]"
                    + " but loggingTransactionInfo value invalid.");
        }

        final TraceRoot traceRoot = item.getTraceRoot();
        final byte loggingTransactionInfo = traceRoot.getShared().getLoggingInfo();
        if (loggingTransactionInfo != loggingInfo.getCode()) {
            LoggingInfo code = LoggingInfo.searchByCode(loggingTransactionInfo);
            if (code != null) {
                throw new AssertionError("Expected a Span isLoggingTransactionInfo value with [" + loggingInfo.getName() + "]"
                        + " but was [" + code.getName() + "]. expected: " + loggingInfo.getName() + ", was: " + code.getName());
            } else {
                throw new AssertionError("Expected a Span isLoggingTransactionInfo value with [" + loggingInfo.getName() + "]"
                        + " but loggingTransactionInfo value invalid.");
            }

        }
    }

    @Override
    public void awaitTrace(ExpectedTrace expectedTrace, long waitUnitTime, long maxWaitTime) {
        if (expectedTrace == null) {
            return;
        }
        Assert.requireNonNull(expectedTrace, "expectedTrace");
        if (waitUnitTime <= 0 || maxWaitTime <= 0) {
            throw new IllegalArgumentException("must be greater than 0");
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        while (true) {
            try {
                if (hasTrace(expectedTrace)) {
                    return;
                }
            } catch (Exception e) {
            }

            try {
                Thread.sleep(waitUnitTime);
            } catch (InterruptedException e) {
            }

            if (stopWatch.stop() > maxWaitTime) {
                // do nothing (it is good to catch the cause.)
                return;
            }
        }
    }

    private boolean hasTrace(ExpectedTrace expectedTrace) {
        if (expectedTrace == null) {
            return false;
        }

        ResolvedExpectedTrace resolvedExpectedTrace = resolveExpectedTrace(expectedTrace, null);

        Iterator<Object> iterator = this.handler.getOrderedSpanRecorder().iterator();
        while (iterator.hasNext()) {
            try {
                Object value = iterator.next();

                ActualTrace actualTrace = null;
                if (value instanceof SpanChunk) {
                    actualTrace = new SpanEventFacade((SpanChunk) value);
                } else if (value instanceof Span) {
                    actualTrace = new SpanFacade((Span) value);
                } else {
                    continue;
                }

                verifySpan(resolvedExpectedTrace, actualTrace);
                return true;
            } catch (Throwable e) {
            }
        }

        return false;
    }

    @Override
    public void awaitTraceCount(int expectedTraceCount, long waitUnitTime, long maxWaitTime) {
        if (waitUnitTime <= 0 || maxWaitTime <= 0) {
            throw new IllegalArgumentException("must be greater than 0");
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        while (true) {
            try {
                verifyTraceCount(expectedTraceCount);
                return;
            } catch (Throwable e) {
            }

            try {
                Thread.sleep(waitUnitTime);
            } catch (InterruptedException e) {
            }

            if (stopWatch.stop() > maxWaitTime) {
                // do nothing (it is good to catch the cause.)
                return;
            }
        }
    }

}
