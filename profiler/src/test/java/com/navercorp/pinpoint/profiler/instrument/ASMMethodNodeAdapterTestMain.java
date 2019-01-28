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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.profiler.instrument.mock.ArgsArrayInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.registry.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.mockito.Mockito.mock;

public class ASMMethodNodeAdapterTestMain {
    private final static InterceptorRegistryBinder interceptorRegistryBinder = new DefaultInterceptorRegistryBinder();
    private int interceptorId;

    public ASMMethodNodeAdapterTestMain() {
        this.interceptorRegistryBinder.bind();
        this.interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(new ArgsArrayInterceptor());
    }

    public void search() throws Exception {
        final String classpath = System.getProperty("java.class.path");
        final String[] paths = classpath.split(";");
        for (String path : paths) {
            if (path.endsWith(".jar")) {
                // searchJar(path);
            } else {
                File file = new File(path);
                if (file.isDirectory()) {
                    searchFile(path, path);
                }
            }
        }
    }

    public void searchFile(final String classPath, final String path) throws Exception {
        File file = new File(path);
        if (file.isDirectory()) {
            for (String name : file.list()) {
                searchFile(classPath, file.getPath() + File.separator + name);
            }
        } else if (path.endsWith(".class")) {
            final String className = path.substring(classPath.length() + 1, path.length() - 6);
            addInterceptor(className.replace(File.separatorChar, '/'));
        }
    }

    public void searchJar(final String path) throws Exception {
        JarFile jar = new JarFile(path);
        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.endsWith(".class")) {
                final String className = name.substring(0, name.length() - 6);
                addInterceptor(className);
            }
        }
    }

    private void addInterceptor(final String className) throws Exception {
        final boolean trace = false;
        final boolean verify = false;

        final String classInternalName = JavaAssistUtils.jvmNameToJavaName(className);
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (!name.startsWith("java") && !name.startsWith("sun") && super.findLoadedClass(name) == null) {
                    try {
                        ClassNode classNode = ASMClassNodeLoader.get(JavaAssistUtils.javaNameToJvmName(name));
                        EngineComponent engineComponent = mock(DefaultEngineComponent.class);
                        ASMClass asmClass = new ASMClass(engineComponent, null, null, null, classNode);
                        if (asmClass.isInterceptable()) {
                            for (InstrumentMethod method : asmClass.getDeclaredMethods()) {
                                try {
                                    method.addInterceptor(interceptorId);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        }

                        byte[] bytes = asmClass.toBytecode();
                        if (trace) {
                            ClassReader classReader = new ClassReader(bytes);
                            ClassWriter cw = new ClassWriter(0);
                            TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                            classReader.accept(tcv, 0);
                        }
                        if (verify) {
                            CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.out));
                        }

                        return super.defineClass(name, bytes, 0, bytes.length);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        return null;
                    }
                } else {
                    try {
                        return super.loadClass(name);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        return null;
                    }
                }

            }
        };

        try {
            classLoader.loadClass(classInternalName);
        } catch (ClassNotFoundException cnfe) {
        }
    }


    public static void main(String[] args) {
        ASMMethodNodeAdapterTestMain main = new ASMMethodNodeAdapterTestMain();
        try {
            main.search();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}