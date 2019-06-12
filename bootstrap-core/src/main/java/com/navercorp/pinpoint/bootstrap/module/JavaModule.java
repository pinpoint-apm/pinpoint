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

package com.navercorp.pinpoint.bootstrap.module;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim - Add addProvides()
 */
public interface JavaModule {
    boolean isSupported();

    boolean isNamed();

    String getName();

    List<Providers> getProviders();

    void addReads(JavaModule target);

    void addExports(String packageName, JavaModule target);

    void addOpens(String packageName, JavaModule target);

    void addUses(Class<?> target);

    void addProvides(Class<?> service, List<Class<?>> providerList);

    boolean isExported(String packageName, JavaModule targetJavaModule);

    boolean isOpen(String packageName, JavaModule targetJavaModule);

    boolean canRead(JavaModule targetJavaModule);

    boolean canRead(Class<?> targetClazz);

    ClassLoader getClassLoader();
}
