/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMClassPool implements InstrumentClassPool {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();
    private final boolean isDebug = logger.isDebugEnabled();

    private final InterceptorRegistryBinder interceptorRegistryBinder;

    public ASMClassPool(final InterceptorRegistryBinder interceptorRegistryBinder, final List<String> bootStrapJars) {
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }

        this.interceptorRegistryBinder = interceptorRegistryBinder;
    }

    @Override
    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) throws NotFoundInstrumentException {
        if (classInternalName == null) {
            throw new NullPointerException("class internal name must not be null.");
        }

        try {
            if (classFileBuffer == null) {
                ASMClassNodeAdapter classNode = ASMClassNodeAdapter.get(classLoader, classInternalName.replace('.', '/'));
                if (classNode == null) {
                    return null;
                }

                return new ASMClass(instrumentContext, interceptorRegistryBinder, classLoader, classNode);
            }

            // Use ASM tree api.
            final ClassReader classReader = new ClassReader(classFileBuffer);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            return new ASMClass(instrumentContext, interceptorRegistryBinder, classLoader, classNode);
        } catch (Exception e) {
            throw new NotFoundInstrumentException(e);
        }
    }

    @Override
    public boolean hasClass(ClassLoader classLoader, String classBinaryName) {
        return classLoader.getResource(classBinaryName.replace('.', '/') + ".class") != null;
    }

    @Override
    public void appendToBootstrapClassPath(String jar) {
        // nothing.
    }
}