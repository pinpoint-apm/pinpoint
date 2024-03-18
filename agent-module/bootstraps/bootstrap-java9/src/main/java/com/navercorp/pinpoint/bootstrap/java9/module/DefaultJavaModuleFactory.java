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

import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultJavaModuleFactory implements JavaModuleFactory {

    private final Instrumentation instrumentation;

    public DefaultJavaModuleFactory(Instrumentation instrumentation) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
    }

    @Override
    public JavaModule wrapFromClass(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return new Java9Module(instrumentation, clazz.getModule());
    }

    @Override
    public JavaModule wrapFromModule(Object module) {
        if (!(module instanceof Module)) {
            throw new IllegalArgumentException("module not java.lang.module");
        }
        return new Java9Module(instrumentation, (Module) module);
    }

    @Override
    public boolean isNamedModule(Object module) {
        if (!(module instanceof Module)) {
            throw new IllegalArgumentException("module not java.lang.module");
        }
        return ((Module) module).isNamed();
    }

    @Override
    public Object getUnnamedModule(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader");

        return classLoader.getUnnamedModule();
    }

    @Override
    public Object getModule(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        return clazz.getModule();
    }
}
