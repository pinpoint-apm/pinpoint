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

package com.navercorp.pinpoint.profiler.context.javamodule;

import com.navercorp.pinpoint.bootstrap.module.ClassFileTransformModuleAdaptor;
import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;
import com.navercorp.pinpoint.common.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerModuleHandler implements ClassFileTransformModuleAdaptor {

    private final Instrumentation instrumentation;
    private final ClassFileTransformer delegate;
    private final JavaModuleFactory javaModuleFactory;
    private final JavaModule bootstrapModule;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public ClassFileTransformerModuleHandler(Instrumentation instrumentation, ClassFileTransformer delegate) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }

        this.instrumentation = instrumentation;
        this.delegate = delegate;
        this.javaModuleFactory = JavaModuleFactoryFinder.lookup();
        this.bootstrapModule = javaModuleFactory.wrapFromClass(instrumentation, JavaModuleFactory.class);
    }

    @Override
    public byte[] transform(Object module, ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        final byte[] transform = delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transform != null && transform != classfileBuffer) {
            if (!javaModuleFactory.isNamedModule(module)) {
                return transform;
            }

            final JavaModule javaModule = javaModuleFactory.wrapFromModule(instrumentation, module);
            if (!javaModule.canRead(bootstrapModule)) {
                if (logger.isInfoEnabled()) {
                    logger.info("addReads module:{} target:{}", javaModule, bootstrapModule);
                }
                javaModule.addReads(bootstrapModule);
            }
            final String packageName = getPackageName(className);
            if (packageName != null) {
                if (!javaModule.isExported(packageName, bootstrapModule)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("addExports module:{} pkg:{} target:{}", javaModule, packageName, bootstrapModule);
                    }
                    javaModule.addExports(packageName, bootstrapModule);
                }
                // need open?
            }
        }
        return transform;
    }

    private String getPackageName(String className) {
        final String packageName = ClassUtils.getPackageName(className, '/', null);
        if (packageName == null) {
            return null;
        }
        return PackageUtils.getPackageNameFromInternalName(className);
    }
}
