/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin.test;

import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;

import java.lang.reflect.Member;

/**
 * @author Jongho Moon
 */
public final class Expectations {

    private static final Object ANY_ANNOTATION_VALUE = new Object();

    private Expectations() {
    }

    public static Object anyAnnotationValue() {
        return ANY_ANNOTATION_VALUE;
    }

    public static ExpectedTrace root(String serviceType, Member method, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(serviceType);
        rootBuilder.setMethod(method);
        rootBuilder.setRpc(rpc);
        rootBuilder.setEndPoint(endPoint);
        rootBuilder.setRemoteAddr(remoteAddr);
        rootBuilder.setAnnotations(annotations);
        return rootBuilder.build();
    }

    public static ExpectedTrace root(String serviceType, Member method, Exception exception, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(serviceType);
        rootBuilder.setMethod(method);
        rootBuilder.setException(exception);
        rootBuilder.setRpc(rpc);
        rootBuilder.setEndPoint(endPoint);
        rootBuilder.setRemoteAddr(remoteAddr);
        rootBuilder.setAnnotations(annotations);
        return rootBuilder.build();
    }

    public static ExpectedTrace root(String serviceType, String methodDescriptor, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(serviceType);
        rootBuilder.setMethodSignature(methodDescriptor);
        rootBuilder.setRpc(rpc);
        rootBuilder.setEndPoint(endPoint);
        rootBuilder.setRemoteAddr(remoteAddr);
        rootBuilder.setAnnotations(annotations);
        return rootBuilder.build();
    }

    public static ExpectedTrace root(String serviceType, String methodDescriptor, Exception exception, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(serviceType);
        rootBuilder.setMethodSignature(methodDescriptor);
        rootBuilder.setException(exception);
        rootBuilder.setRpc(rpc);
        rootBuilder.setEndPoint(endPoint);
        rootBuilder.setRemoteAddr(remoteAddr);
        rootBuilder.setAnnotations(annotations);
        return rootBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, Member method, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethod(method);
        eventBuilder.setRpc(rpc);
        eventBuilder.setEndPoint(endPoint);
        eventBuilder.setDestinationId(destinationId);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, Member method, Exception exception, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethod(method);
        eventBuilder.setException(exception);
        eventBuilder.setRpc(rpc);
        eventBuilder.setEndPoint(endPoint);
        eventBuilder.setDestinationId(destinationId);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, Member method, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethod(method);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, Member method, Exception exception, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethod(method);
        eventBuilder.setException(exception);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, String methodDescriptor, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethodSignature(methodDescriptor);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, String methodDescriptor, Exception exception, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethodSignature(methodDescriptor);
        eventBuilder.setException(exception);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, String methodDescriptor, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethodSignature(methodDescriptor);
        eventBuilder.setRpc(rpc);
        eventBuilder.setEndPoint(endPoint);
        eventBuilder.setDestinationId(destinationId);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace event(String serviceType, String methodDescriptor, Exception exception, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(serviceType);
        eventBuilder.setMethodSignature(methodDescriptor);
        eventBuilder.setException(exception);
        eventBuilder.setRpc(rpc);
        eventBuilder.setEndPoint(endPoint);
        eventBuilder.setDestinationId(destinationId);
        eventBuilder.setAnnotations(annotations);
        return eventBuilder.build();
    }

    public static ExpectedTrace async(ExpectedTrace initiator, ExpectedTrace... asyncTraces) {
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createBuilder(initiator.getType(), initiator.getServiceType());
        eventBuilder.setMethod(initiator.getMethod());
        eventBuilder.setMethodSignature(initiator.getMethodSignature());
        eventBuilder.setException(initiator.getException());
        eventBuilder.setRpc(initiator.getRpc());
        eventBuilder.setEndPoint(initiator.getEndPoint());
        eventBuilder.setRemoteAddr(initiator.getRemoteAddr());
        eventBuilder.setDestinationId(initiator.getDestinationId());
        eventBuilder.setAnnotations(initiator.getAnnotations());
        eventBuilder.setAsyncTraces(asyncTraces);
        return eventBuilder.build();
    }

    public static ExpectedAnnotation[] annotations(ExpectedAnnotation... annotations) {
        return annotations;
    }

    public static ExpectedAnnotation annotation(String annotationKeyName, Object value) {
        return new ExpectedAnnotation(annotationKeyName, value);
    }

    public static ExpectedAnnotation[] args(Object... args) {
        ExpectedAnnotation[] annotations = new ExpectedAnnotation[args.length];

        for (int i = 0; i < args.length; i++) {
            annotations[i] = annotation(AnnotationKeyUtils.getArgs(i).getName(), args[i]);
        }

        return annotations;
    }

    public static ExpectedAnnotation[] cachedArgs(Object... args) {
        ExpectedAnnotation[] annotations = new ExpectedAnnotation[args.length];

        for (int i = 0; i < args.length; i++) {
            annotations[i] = annotation(AnnotationKeyUtils.getCachedArgs(i).getName(), args[i]);
        }

        return annotations;
    }

    public static ExpectedAnnotation sql(String query, String output, Object... bindValues) {
        return new ExpectedSql(query, output, bindValues);
    }
}
