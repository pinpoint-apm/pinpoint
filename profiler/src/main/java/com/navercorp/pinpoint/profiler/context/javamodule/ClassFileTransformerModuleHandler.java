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

    private final ClassFileTransformer delegate;
    private final JavaModuleFactory javaModuleFactory;
    private final JavaModule bootstrapModule;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public ClassFileTransformerModuleHandler(Instrumentation instrumentation, ClassFileTransformer delegate, JavaModuleFactory javaModuleFactory) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }
        if (javaModuleFactory == null) {
            throw new NullPointerException("javaModuleFactory");
        }
        this.delegate = delegate;
        this.javaModuleFactory = javaModuleFactory;
        this.bootstrapModule = javaModuleFactory.wrapFromClass(JavaModuleFactory.class);
    }

    @Override
    public byte[] transform(Object transformedModuleObject, ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        final byte[] transform = delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transformedModuleObject == null) {
            return transform;
        }
        if (transform != null && transform != classfileBuffer) {
            if (!javaModuleFactory.isNamedModule(transformedModuleObject)) {
                return transform;
            }
            // bootstrap-core permission
            final JavaModule transformedModule = javaModuleFactory.wrapFromModule(transformedModuleObject);
            addModulePermission(transformedModule, className, bootstrapModule);


            if (loader != Object.class.getClassLoader()) {
                // plugin permission
                final Object pluginModuleObject = getPluginModule(loader);
                final JavaModule pluginModule = javaModuleFactory.wrapFromModule(pluginModuleObject);

                addModulePermission(transformedModule, className, pluginModule);
            }
        }
        return transform;
    }

    private Object getPluginModule(ClassLoader loader) {
        // current internal implementation
        // The plugin.jar is loaded into the unnamed module
        return javaModuleFactory.getUnnamedModule(loader);
    }

    private void addModulePermission(JavaModule transformedModule, String className, JavaModule targetModule) {
        if (!transformedModule.canRead(targetModule)) {
            if (logger.isInfoEnabled()) {
                logger.info("addReads module:{} target:{}", transformedModule, targetModule);
            }
            transformedModule.addReads(targetModule);
        }

        final String packageName = getPackageName(className);
        if (packageName != null) {
            if (!transformedModule.isExported(packageName, targetModule)) {
                if (logger.isInfoEnabled()) {
                    logger.info("addExports module:{} pkg:{} target:{}", transformedModule, packageName, targetModule);
                }
                transformedModule.addExports(packageName, targetModule);
            }
            // need open?
        }
    }

    private String getPackageName(String className) {
        final String packageName = ClassUtils.getPackageName(className, '/', null);
        if (packageName == null) {
            return null;
        }
        return PackageUtils.getPackageNameFromInternalName(className);
    }
}
