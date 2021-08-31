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
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.StopWatch;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.test.util.AnnotationUtils;
import com.navercorp.pinpoint.test.util.AssertionErrorBuilder;
import com.navercorp.pinpoint.test.util.ObjectUtils;
import com.navercorp.pinpoint.test.util.ThreadUtils;
import com.navercorp.pinpoint.test.wrapper.ActualTrace;
import com.navercorp.pinpoint.test.wrapper.ActualTraceFactory;

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
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginVerifierExternalAdaptor implements PluginTestVerifier {

    private final List<Short> ignoredServiceTypes = new ArrayList<>();

    private final DefaultApplicationContext applicationContext;

    private final ApplicationContextHandler handler;

    public PluginVerifierExternalAdaptor(DefaultApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
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
            AssertionErrorBuilder builder = new AssertionErrorBuilder("serverType", expectedType, actualType);
            builder.throwAssertionError();
        }
    }

    @Override
    public void verifyServerInfo(String serverInfo) {
        String actualName = getServerMetaData().getServerInfo();

        if (!actualName.equals(serverInfo)) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder("serverInfo", serverInfo, actualName);
            builder.throwAssertionError();
        }
    }

    @Override
    public void verifyConnector(String protocol, int port) {
        Map<Integer, String> connectorMap = getServerMetaData().getConnectors();
        String actualProtocol = connectorMap.get(port);

        if (actualProtocol == null || !actualProtocol.equals(protocol)) {
            // port validation??
            AssertionErrorBuilder builder = new AssertionErrorBuilder("protocol", protocol + ":" + port, actualProtocol);
            builder.throwAssertionError();
        }
    }

    @Override
    public void verifyService(String serverName, List<String> libs) {
        List<ServiceInfo> serviceInfos = getServerMetaData().getServiceInfos();

        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo.getServiceName().equals(serverName)) {
                List<String> actualLibs = serviceInfo.getServiceLibs();

                if (actualLibs.size() != libs.size()) {
                    AssertionErrorBuilder builder = new AssertionErrorBuilder("serviceName " + serverName, libs, actualLibs);
                    builder.throwAssertionError();
                }

                for (String lib : libs) {
                    if (!actualLibs.contains(lib)) {
                        AssertionErrorBuilder builder = new AssertionErrorBuilder("serviceName " + serverName, libs, actualLibs);
                        builder.throwAssertionError();
                    }
                }

                // OK
                return;
            }
        }

        AssertionErrorBuilder builder = new AssertionErrorBuilder("serviceName " + serverName, libs, "no such service");
        builder.throwAssertionError();
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
        final int actual = getTraceCount();

        if (expected != actual) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder("count", expected, actual);
            builder.throwAssertionError();
        }
    }

    @Override
    public int getTraceCount() {
        int actual = 0;
        for (Object obj : this.handler.getOrderedSpanRecorder()) {
            if (!isIgnored(obj)) {
                actual++;
            }
        }
        return actual;
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
            ActualTrace actual = ActualTraceFactory.wrap(next);

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

            final Item actualItem = popItem();
            if (actualItem == null) {
                AssertionErrorBuilder builder = new AssertionErrorBuilder("actualItem is null", resolved, "null");
                builder.throwAssertionError();
            }
            final Object actual = actualItem.getValue();

            ActualTrace wrapped = ActualTraceFactory.wrap(actual);

            verifySpan(resolved, wrapped);
            verifyAsyncTraces(expected, wrapped);
        }
    }

    private void verifyAsyncTraces(ExpectedTrace expected, ActualTrace actual) throws AssertionError {
        final ExpectedTrace[] expectedAsyncTraces = expected.getAsyncTraces();

        if (expectedAsyncTraces != null && expectedAsyncTraces.length > 0) {
            Integer actualAsyncId = actual.getNextAsyncId();

            if (actualAsyncId == null) {
                AssertionErrorBuilder builder = new AssertionErrorBuilder("async traces triggered but nextAsyncId is null",
                        Arrays.toString(expectedAsyncTraces), actualAsyncId);
                builder.setComparison(expected, actual);
                builder.throwAssertionError();
            }

            verifyDiscreteTraceBlock(expectedAsyncTraces, actualAsyncId);
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
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + " InstanceType",
                    expected.type.getSimpleName(), actual.getType().getName());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!equals(expected.serviceType.getCode(), actual.getServiceType())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".serviceType",
                    expected.serviceType.getCode(), actual.getServiceType());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }


        if (!equals(expected.apiId, actual.getApiId())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".apiId",
                    expected.apiId, actual.getApiId());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!equals(expected.rpc, actual.getRpc())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".rpc",
                    expected.rpc, actual.getRpc());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!equals(expected.endPoint, actual.getEndPoint())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".endPoint",
                    expected.endPoint, actual.getEndPoint());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!equals(expected.remoteAddr, actual.getRemoteAddr())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".remoteAddr",
                    expected.remoteAddr, actual.getRemoteAddr());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!equals(expected.destinationId, actual.getDestinationId())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".destinationId",
                    expected.destinationId, actual.getDestinationId());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!equals(getAsyncId(expected), actual.getAsyncId())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".asyncId",
                    expected.localAsyncId, actual.getAsyncId());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (expected.exception != null) {
            final IntStringValue actualExceptionInfo = actual.getExceptionInfo();
            if (actualExceptionInfo != null) {
                String actualExceptionClassName = this.handler.getTcpDataSender().getString(actualExceptionInfo.getIntValue());
                String actualExceptionMessage = actualExceptionInfo.getStringValue();
                verifyException(expected.exception, actualExceptionClassName, actualExceptionMessage);
            } else {
                AssertionErrorBuilder builder = new AssertionErrorBuilder(expected.type.getSimpleName() + ".exception",
                        expected.exception.getClass().getName(), null);
                builder.throwAssertionError();
            }
        }

        List<Annotation<?>> actualAnnotations = actual.getAnnotations();

        final int expectedLen = ArrayUtils.getLength(expected.annotations);
        final int actualLen = CollectionUtils.nullSafeSize(actualAnnotations);

        if (actualLen != expectedLen) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder("Annotation.length", expectedLen, actualLen);
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        for (int i = 0; i < expectedLen; i++) {
            annotationCompare(i, expected, actual, actualAnnotations.get(i));
        }
    }

    private void annotationCompare(int index, ResolvedExpectedTrace expected, ActualTrace actual, Annotation<?> actualAnnotation) {
        final ExpectedAnnotation expect = expected.annotations[index];
        final AnnotationKey expectedAnnotationKey = this.handler.getAnnotationKeyRegistryService().findAnnotationKeyByName(expect.getKeyName());

        if (expectedAnnotationKey.getCode() != actualAnnotation.getKey()) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(String.format("Annotation[%s].key", index),
                    AnnotationUtils.toString(expectedAnnotationKey, expect), AnnotationUtils.toString(actualAnnotation));
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (expectedAnnotationKey == AnnotationKey.SQL_ID && expect instanceof ExpectedSql) {
            verifySql(index, (ExpectedSql) expect, actualAnnotation);
        } else if (expect.getValue() instanceof DataType) {
            verifyDataType(index, ((DataType) expect.getValue()), actualAnnotation);
        } else {
            Object expectedValue = expect.getValue();

            if (expectedValue == Expectations.anyAnnotationValue()) {
                return;
            }

            if (AnnotationKeyUtils.isCachedArgsKey(expectedAnnotationKey.getCode())) {
                expectedValue = this.handler.getTcpDataSender().getStringId(expectedValue.toString());
            }

            if (!ObjectUtils.equals(expectedValue, actualAnnotation.getValue())) {
                AssertionErrorBuilder builder = new AssertionErrorBuilder(String.format("Annotation[%s].value", index),
                        expectedAnnotationKey.getCode(), AnnotationUtils.toString(actualAnnotation));
                builder.setComparison(expected, actual);
                builder.throwAssertionError();
            }
        }
    }

    private void verifyDataType(int index, DataType expectedValue, Annotation<?> actualAnnotation) {
        DataType annotationValue = (DataType) actualAnnotation.getValue();

        if (!ObjectUtils.equals(expectedValue, annotationValue)) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(String.format("Annotation[%s].value", index),
                    expectedValue, annotationValue);
            builder.throwAssertionError();
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
        if (!ObjectUtils.equals(actualExceptionClassName, expectedExceptionClassName)) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder("ExceptionClassName",
                    expectedExceptionClassName, actualExceptionClassName);
            builder.throwAssertionError();
        }
        if (!ObjectUtils.equals(actualExceptionMessage, expectedExceptionMessage)) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder("Exception Message",
                    expectedExceptionMessage, actualExceptionMessage);
            builder.throwAssertionError();
        }
    }

    private void verifySql(int index, ExpectedSql expected, Annotation<?> actual) {
        int id = this.handler.getTcpDataSender().getSqlId(expected.getQuery());
        IntStringStringValue actualSql = (IntStringStringValue) actual.getValue();

        if (actualSql.getIntValue() != id) {
            String actualQuery = this.handler.getTcpDataSender().getSql(actualSql.getIntValue());

            AssertionErrorBuilder builder = new AssertionErrorBuilder(String.format("Annotation[%s].sqlId", index),
                    id + ":" + expected.getQuery(), actualSql.getIntValue() + ": " + actualQuery);
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!ObjectUtils.equals(actualSql.getStringValue1(), expected.getOutput())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(String.format("Annotation[%s].sql.output", index),
                    expected.getOutput(), actualSql.getStringValue1());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
        }

        if (!ObjectUtils.equals(actualSql.getStringValue2(), expected.getBindValuesAsString())) {
            AssertionErrorBuilder builder = new AssertionErrorBuilder(String.format("Annotation[%s].sql.bindValues", index),
                    expected.getBindValuesAsString(), actualSql.getStringValue2());
            builder.setComparison(expected, actual);
            builder.throwAssertionError();
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
    public void printMethod() {
        List<String> executedMethod = this.handler.getExecutedMethod();
        System.out.println("Method(" + executedMethod.size() + ")");
        for (String method : executedMethod) {
            System.out.println(method);
        }

    }

    @Override
    public List<String> getExecutedMethod() {
        return this.handler.getExecutedMethod();
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
            String codeName = getCodeName(code);
            AssertionErrorBuilder builder = new AssertionErrorBuilder("Span.isLoggingTransactionInfo value",
                    loggingInfo.getName(), codeName);
            builder.setComparison(loggingInfo.getName(), codeName);
            builder.throwAssertionError();
        }
    }

    private String getCodeName(LoggingInfo code) {
        if (code == null) {
            return null;
        }
        return code.getName();
    }

    @Override
    public void awaitTrace(ExpectedTrace expectedTrace, long waitUnitTime, long maxWaitTime) {
        if (expectedTrace == null) {
            return;
        }
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
            } catch (Exception ignore) {
            }

            ThreadUtils.sleep(waitUnitTime);

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

                ActualTrace actualTrace = ActualTraceFactory.wrapOrNull(value);
                if (actualTrace == null) {
                    continue;
                }

                verifySpan(resolvedExpectedTrace, actualTrace);
                return true;
            } catch (Throwable ignore) {
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
            } catch (Throwable ignore) {
            }

            ThreadUtils.sleep(waitUnitTime);

            if (stopWatch.stop() > maxWaitTime) {
                // do nothing (it is good to catch the cause.)
                return;
            }
        }
    }

}
