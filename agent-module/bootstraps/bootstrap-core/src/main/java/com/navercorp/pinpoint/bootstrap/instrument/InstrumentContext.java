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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

import java.io.InputStream;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public interface InstrumentContext extends ClassInputStreamProvider {

    InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classfileBuffer);

    boolean exist(ClassLoader classLoader, String className, ProtectionDomain protectionDomain);

    InterceptorScope getInterceptorScope(String name);

    <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className);

    @Override
    InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath);

    void addClassFileTransformer(ClassLoader classLoader, String targetClassName, TransformCallback transformCallback);

    void addClassFileTransformer(ClassLoader classLoader, String targetClassName, String transformCallbackClassName);

    void addClassFileTransformer(Matcher matcher, TransformCallback transformCallback);

    void addClassFileTransformer(Matcher matcher, String transformCallbackClassName);

    void addClassFileTransformer(Matcher matcher, String transformCallbackClassName, Object[] parameters, Class<?>[] parameterTypes);

    void retransform(Class<?> target, TransformCallback transformCallback);

}
