/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.classloading;

/**
 * JDK 8 only. ClassLoader.defineClass cannot target the bootstrap classloader,
 * so a null classloader is routed to Unsafe.defineClass.
 */
final class Java8DefineClass implements DefineClass {

    private final DefineClass defineClass = new ReflectionDefineClass();

    // UnsafeDefineClass fails to initialize where Unsafe.defineClass is unavailable;
    // initialize it lazily so such a failure cannot break the non-bootstrap path.
    private static class BootstrapDefineClassHolder {
        static final DefineClass INSTANCE = new UnsafeDefineClass();
    }

    @Override
    public Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
        if (classLoader == null) {
            return BootstrapDefineClassHolder.INSTANCE.defineClass(null, name, bytes);
        }
        return defineClass.defineClass(classLoader, name, bytes);
    }
}
