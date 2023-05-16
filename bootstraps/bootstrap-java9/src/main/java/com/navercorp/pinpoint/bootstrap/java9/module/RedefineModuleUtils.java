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
import java.util.Objects;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
final class RedefineModuleUtils {

    private RedefineModuleUtils() {
    }

    static void addReads(Instrumentation instrumentation, Module module, Set<Module> extraReads) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraReads, "extraReads");

        // for debug
//        final Set<Module> extraReads0 = extraReads;
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addExports(Instrumentation instrumentation, Module module, Map<String, Set<Module>> extraExports) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraExports, "extraExports");

        // for debug
        final Set<Module> extraReads = Set.of();
//        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    static void addOpens(Instrumentation instrumentation, Module module, Map<String, Set<Module>> extraOpens) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraOpens, "extraOpens");

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
//        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }

    public static void addUses(Instrumentation instrumentation, Module module, Set<Class<?>> extraUses) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraUses, "extraUses");

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
//        final Set<Class<?>> extraUses = Set.of();
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);

    }

    public static void addProvides(Instrumentation instrumentation, Module module, Map<Class<?>, List<Class<?>>> extraProvides) {
        Objects.requireNonNull(instrumentation, "instrumentation");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(extraProvides, "extraProvides");

        // for debug
        final Set<Module> extraReads = Set.of();
        final Map<String, Set<Module>> extraExports = Map.of();
        final Map<String, Set<Module>> extraOpens = Map.of();
        final Set<Class<?>> extraUses = Set.of();
        instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides);
    }
}
