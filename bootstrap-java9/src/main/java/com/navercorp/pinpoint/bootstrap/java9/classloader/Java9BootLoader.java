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

package com.navercorp.pinpoint.bootstrap.java9.classloader;


import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Woonduk Kang(emeroad)
 */
class Java9BootLoader {

    Java9BootLoader() {
    }

    Enumeration<URL> findResources(String name) throws IOException {
        return jdk.internal.loader.BootLoader.findResources(name);
    }

    URL findResource(String name) {
        return jdk.internal.loader.BootLoader.findResource(name);
    }

    Class<?> findBootstrapClassOrNull(ClassLoader classLoader, String name) {
        return jdk.internal.loader.BootLoader.loadClassOrNull(name);
    }

    // SharedSecrets version
//    private final jdk.internal.misc.JavaLangAccess javaLangAccess = jdk.internal.misc.SharedSecrets.getJavaLangAccess();
//    @Override
//    public Class<?> findBootstrapClassOrNull(ClassLoader classLoader, String name) {
//        return javaLangAccess.findBootstrapClassOrNull(classLoader, name);
//    }
}
