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

package com.navercorp.pinpoint.bootstrap.java9.module;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
final class RedefineModuleUtils {

    private RedefineModuleUtils() {
    }

    static void addReads(Instrumentation instrumentation, Module module, Set<Module> extraReads) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (extraReads == null) {
            throw new NullPointerException("extraReads");
        }

        // for debug
//        final Set<Module> extraReads0 = extraReads;
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addExports(Instrumentation instrumentation, Module module, Map<String, Set<Module>> extraExports) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (extraExports == null) {
            throw new NullPointerException("extraExports");
        }

        // for debug
        final Set<Module> extraReads = Set.of();
//        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addOpens(Instrumentation instrumentation, Module module, Map<String, Set<Module>> extraOpens) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (extraOpens == null) {
            throw new NullPointerException("extraOpens");
        }

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
//        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    public static void addUses(Instrumentation instrumentation, Module module, Set<Class<?>> extraUses) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (extraUses == null) {
            throw new NullPointerException("extraUses");
        }

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
//        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);

    }

    public static void addProvides(Instrumentation instrumentation, Module module, Map<Class<?>, List<Class<?>>> extraProvides) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (extraProvides == null) {
            throw new NullPointerException("extraProvides");
        }

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }
}
