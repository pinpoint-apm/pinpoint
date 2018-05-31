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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9ClassFileTransformer implements ClassFileTransformer {

    private final ModuleLogger logger = ModuleLogger.getLogger(this.getClass().getName());

    private final ClassFileTransformer delegate;
    private final Module bootstrapModule;
    private final Set<Module> bootstrapModuleSet;
    private final Instrumentation instrumentation;

    public static ClassFileTransformer wrap(Instrumentation instrumentation, ClassFileTransformer delegate, Class<?> bootstrapClass) {
        return new Java9ClassFileTransformer(instrumentation, delegate, bootstrapClass);
    }

    private Java9ClassFileTransformer(Instrumentation instrumentation, ClassFileTransformer delegate, Class<?> bootstrapClass) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        if (bootstrapClass == null) {
            throw new NullPointerException("bootstrapClass must not be null");
        }
        this.instrumentation = instrumentation;
        this.delegate = delegate;
        this.bootstrapModule = bootstrapClass.getModule();
        this.bootstrapModuleSet = Set.of(bootstrapModule);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        final byte[] transform = delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transform != null && transform != classfileBuffer) {
            if (module.isNamed()) {
                if (!module.canRead(bootstrapModule)) {
                    RedefineModuleUtils.addReads(instrumentation, module, bootstrapModuleSet);
                }
                final String packageName = PackageUtils.getPackageNameFromInternalName(className);
                if (packageName != null) {
                    if (!module.isExported(packageName, bootstrapModule)) {
                        Map<String, Set<Module>> extraExports = Map.of(packageName, bootstrapModuleSet);
                        RedefineModuleUtils.addExports(instrumentation, module, extraExports);
                    }
                    // need open?
                }
            }
        }
        return transform;
    }



}
