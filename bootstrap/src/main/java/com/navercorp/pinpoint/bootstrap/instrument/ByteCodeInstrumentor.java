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

package com.navercorp.pinpoint.bootstrap.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;

/**
 * @author emeroad
 */
public interface ByteCodeInstrumentor {

    InstrumentClass getClass(ClassLoader classLoader, String jvmClassName, byte[] classFileBuffer) throws NotFoundInstrumentException;
    
    boolean findClass(ClassLoader classLoader, String javassistClassName);

    @Deprecated
    InterceptorGroupInvocation getInterceptorGroupTransaction(String scopeName);

    @Deprecated
    InterceptorGroupInvocation getInterceptorGroupTransaction(InterceptorGroupDefinition scopeDefinition);

    @Deprecated
    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException;

//    TargetMethod newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params) throws InstrumentException;

    @Deprecated
    Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException;
    
    void retransform(Class<?> target, ClassFileTransformer classEditor);

    RetransformEventTrigger getRetransformEventTrigger();
}
