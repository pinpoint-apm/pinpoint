/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.plugin.transformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.plugin.PluginClassLoaderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.DedicatedClassFileTransformer;
import com.navercorp.pinpoint.exception.PinpointException;

public class DefaultDedicatedClassFileTransformer implements DedicatedClassFileTransformer {
    private final ByteCodeInstrumentor instrumentor;
    private final PluginClassLoaderFactory classLoaderFactory;

    private final String targetClassName;
    private final ClassRecipe recipe;
    
    
    public DefaultDedicatedClassFileTransformer(ByteCodeInstrumentor instrumentor, PluginClassLoaderFactory classLoaderFactory, String targetClassName, ClassRecipe recipe) {
        this.instrumentor = instrumentor;
        this.classLoaderFactory = classLoaderFactory;

        this.targetClassName = targetClassName;
        this.recipe = recipe;
    }
    
    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassLoader forPlugin = classLoaderFactory.get(classLoader);
        
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(forPlugin);
        
        try {
            InstrumentClass target = instrumentor.getClass(classLoader, className, classfileBuffer);
            recipe.edit(forPlugin, target);
            return target.toBytecode();
        } catch (PinpointException e) {
            throw e;
        } catch (Throwable e) {
            String msg = "Fail to invoke plugin class recipe: " + toString();
            throw new PinpointException(msg, e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public String getTargetClassName() {
        return targetClassName;
    }

    @Override
    public String toString() {
        return "ClassEditor[target=" + targetClassName + ", subEditors=" + recipe + "]";
    }
}
