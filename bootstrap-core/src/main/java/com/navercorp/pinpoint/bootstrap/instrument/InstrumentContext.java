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

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public interface InstrumentContext {

    InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classfileBuffer);

    boolean exist(ClassLoader classLoader, String className);

    InterceptorScope getInterceptorScope(String name);

    <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className);

    InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath);

    void addClassFileTransformer(ClassLoader classLoader, String targetClassName, TransformCallback transformCallback);

    void addClassFileTransformer(Matcher matcher, TransformCallback transformCallback);

    void retransform(Class<?> target, TransformCallback transformCallback);

}
