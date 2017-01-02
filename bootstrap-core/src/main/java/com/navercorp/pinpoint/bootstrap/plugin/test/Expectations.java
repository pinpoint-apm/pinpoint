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

import java.lang.reflect.Member;

import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;

/**
 * @author Jongho Moon
 *
 */
public final class Expectations {
    
    private static final Object ANY_ANNOTATION_VALUE = new Object();
    
    
    private Expectations() {}
    
    public static Object anyAnnotationValue() {
        return ANY_ANNOTATION_VALUE;
    }
    
    public static ExpectedTrace root(String serviceType, Member method, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        return new ExpectedTrace(TraceType.ROOT, serviceType, method, null, rpc, endPoint, remoteAddr, null, annotations, null);
    }
    
    public static ExpectedTrace root(String serviceType, String methodDescriptor, String rpc, String endPoint, String remoteAddr, ExpectedAnnotation... annotations) {
        return new ExpectedTrace(TraceType.ROOT, serviceType, null, methodDescriptor, rpc, endPoint, remoteAddr, null, annotations, null);
    }
    
    public static ExpectedTrace event(String serviceType, Member method, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        return new ExpectedTrace(TraceType.EVENT, serviceType, method, null, rpc, endPoint, null, destinationId, annotations, null);
    }
    
    public static ExpectedTrace event(String serviceType, Member method, ExpectedAnnotation... annotations) {
        return new ExpectedTrace(TraceType.EVENT, serviceType, method, null, null, null, null, null, annotations, null);
    }

    public static ExpectedTrace event(String serviceType, String methodDescriptor, ExpectedAnnotation... annotations) {
        return new ExpectedTrace(TraceType.EVENT, serviceType, null, methodDescriptor, null, null, null, null, annotations, null);
    }

    public static ExpectedTrace event(String serviceType, String methodDescriptor, String rpc, String endPoint, String destinationId, ExpectedAnnotation... annotations) {
        return new ExpectedTrace(TraceType.EVENT, serviceType, null, methodDescriptor, rpc, endPoint, null, destinationId, annotations, null);
    }
    
    public static ExpectedTrace async(ExpectedTrace initiator, ExpectedTrace... asyncTraces) {
        return new ExpectedTrace(initiator.getType(), initiator.getServiceType(), initiator.getMethod(), initiator.getMethodSignature(), initiator.getRpc(), initiator.getEndPoint(), initiator.getRemoteAddr(), initiator.getDestinationId(), initiator.getAnnotations(), asyncTraces);
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
