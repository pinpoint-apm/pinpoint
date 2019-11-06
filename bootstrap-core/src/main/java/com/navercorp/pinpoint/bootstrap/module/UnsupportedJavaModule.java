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

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
class UnsupportedJavaModule implements JavaModule {
    public static JavaModule INSTANCE = new UnsupportedJavaModule();

    UnsupportedJavaModule() {
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public boolean isNamed() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<Providers> getProviders() {
        return Collections.emptyList();
    }

    @Override
    public void addReads(JavaModule target) {
        // non
    }

    @Override
    public void addExports(String packageName, JavaModule target) {
        // non
    }

    @Override
    public void addOpens(String packageName, JavaModule target) {
        // non
    }

    @Override
    public void addUses(Class<?> target) {
        // non
    }

    @Override
    public void addProvides(Class<?> service, List<Class<?>> providerList) {
    }

    @Override
    public boolean isExported(String packageName, JavaModule targetJavaModule) {
        return false;
    }

    @Override
    public boolean isOpen(String packageName, JavaModule targetJavaModule) {
        return false;
    }

    @Override
    public boolean canRead(JavaModule targetJavaModule) {
        return false;
    }

    @Override
    public boolean canRead(Class<?> targetClazz) {
        return false;
    }

    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("getClassLoader()");
    }

    @Override
    public String toString() {
        return "UnsupportedJavaModule";
    }
}
