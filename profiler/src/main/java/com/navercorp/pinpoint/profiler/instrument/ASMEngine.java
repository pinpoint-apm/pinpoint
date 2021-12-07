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
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import java.util.Objects;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

/**
 * @author jaehong.kim
 */
public class ASMEngine implements InstrumentEngine {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();

    private final Instrumentation instrumentation;
    private final EngineComponent engineComponent;


    public ASMEngine(Instrumentation instrumentation, EngineComponent engineComponent) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.engineComponent = Objects.requireNonNull(engineComponent, "engineComponent");
    }

    @Override
    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws NotFoundInstrumentException {
        Objects.requireNonNull(className, "className");

        try {
            if (classFileBuffer == null) {
                final ASMClassNodeAdapter classNode = ASMClassNodeAdapter.get(instrumentContext, classLoader, protectionDomain, JavaAssistUtils.javaNameToJvmName(className));
                if (classNode == null) {
                    return null;
                }
                return new ASMClass(engineComponent, instrumentContext, classNode);
            }

            // Use ASM tree api.
            final ClassReader classReader = new ClassReader(classFileBuffer);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);


            return new ASMClass(engineComponent, instrumentContext, classLoader, protectionDomain, classNode);
        } catch (Exception e) {
            throw new NotFoundInstrumentException(e);
        }
    }


    @Override
    public void appendToBootstrapClassPath(JarFile jarFile) {
        Objects.requireNonNull(jarFile, "jarFile");

        if (isInfo) {
            logger.info("appendToBootstrapClassPath:{}", jarFile.getName());
        }
        instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
    }
}