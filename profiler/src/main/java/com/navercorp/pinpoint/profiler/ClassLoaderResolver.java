/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class ClassLoaderResolver {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public Class<? extends ClassLoader> resolve(Instrumentation inst) {
        String pluginClassLoaderName = null;

        String specified = System.getProperty("com.navercorp.pinpoint.plugin.classloader");
        
        if (specified == null) {
            pluginClassLoaderName = resolveClassLoaderType();
        } else {
            pluginClassLoaderName = specified;
        }
        
        logger.info("Use plugin class loader: " + pluginClassLoaderName);

        boolean redefineClassLoader = "com.navercorp.pinpoint.profiler.plugin.Java6PluginClassLoader".equals(pluginClassLoaderName);
        
        try {
            if (redefineClassLoader) {
                retransformClassLoader(inst);
            }

            return Class.forName(pluginClassLoaderName).asSubclass(ClassLoader.class);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError("Cannot find plugin class loader: " + pluginClassLoaderName);
        } catch (Exception e) {
            throw new PinpointException("Failed to prepare " + pluginClassLoaderName, e);
        }
    }

    private String resolveClassLoaderType() {
        String pluginClassLoaderName;
        boolean java6 = isJava6();
        boolean unsafe = hasUnsafe();

        if (java6) {
            if (unsafe) {
                pluginClassLoaderName = "com.navercorp.pinpoint.profiler.plugin.Java6UnsafePluginClassLoader";
            } else {
                pluginClassLoaderName = "com.navercorp.pinpoint.profiler.plugin.Java6PluginClassLoader";
            }
        } else {
            pluginClassLoaderName = "com.navercorp.pinpoint.profiler.plugin.Java7PluginClassLoader";
        }
        return pluginClassLoaderName;
    }

    private void retransformClassLoader(Instrumentation inst) throws UnmodifiableClassException {
        ClassLoaderTransformer transformer = new ClassLoaderTransformer();
        inst.addTransformer(transformer, true);
        inst.retransformClasses(ClassLoader.class);
        inst.removeTransformer(transformer);
    }

    private boolean hasUnsafe() {
        try {
            Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe").setAccessible(true);
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }

    private boolean isJava6() {
        try {
            ClassLoader.class.getDeclaredMethod("registerAsParallelCapable");
        } catch (Exception e) {
            return true;
        }
        
        return false;
    }
    
    private static class ClassLoaderTransformer implements ClassFileTransformer {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            logger.info("in transform: " + className);
            
            if (className.equals("java/lang/ClassLoader")) {
                logger.info("Transform ClassLoader");
                
                try {
                    ClassPool classPool = new ClassPool(true);
                    CtClass c = classPool.get("java.lang.ClassLoader");
                    CtMethod m = c.getMethod("loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;");
                    
                    m.insertBefore("com.navercorp.pinpoint.bootstrap.ClassLoaderLock.lock(this);");
                    m.insertAfter("com.navercorp.pinpoint.bootstrap.ClassLoaderLock.unlock(this);", true);
                 
                    logger.info("Transform ClassLoader Finished");
                    
                    return c.toBytecode();
                } catch (Exception e) {
                    return null;
                }
            }
            
            return null;
        }
        
    }
}
