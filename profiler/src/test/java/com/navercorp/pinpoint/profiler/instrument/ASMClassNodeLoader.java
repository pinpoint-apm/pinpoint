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

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author jaehong.kim
 */
public class ASMClassNodeLoader {

    private final static DefaultProfilerPluginContext pluginContext = mock(DefaultProfilerPluginContext.class);
    private final static TraceContext traceContext = mock(TraceContext.class);

    static {
        reset(traceContext);
        when(pluginContext.getTraceContext()).thenReturn(traceContext);
        when(pluginContext.injectClass(any(ClassLoader.class), any(String.class))).thenAnswer(new Answer<Class<?>>() {

            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader loader = (ClassLoader) invocation.getArguments()[0];
                String name = (String) invocation.getArguments()[1];

                return loader.loadClass(name);
            }

        });
        when(pluginContext.getResourceAsStream(any(ClassLoader.class), any(String.class))).thenAnswer(new Answer<InputStream>() {

            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader loader = (ClassLoader) invocation.getArguments()[0];
                String name = (String) invocation.getArguments()[1];
                if(loader == null) {
                    loader = ClassLoader.getSystemClassLoader();
                }

                return loader.getResourceAsStream(name);
            }
        });
    }


    // only use for test.
    public static ClassNode get(final String classInternalName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassReader cr = new ClassReader(classLoader.getResourceAsStream(JavaAssistUtils.javaNameToJvmName(classInternalName) + ".class"));
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        return classNode;
    }

    public static MethodNode get(final String classInternalName, final String methodName) throws Exception {
        ClassNode classNode = get(classInternalName);
        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            if (methodNode.name.equals(methodName)) {
                return methodNode;
            }
        }

        return null;
    }

    public static TestClassLoader getClassLoader() {
        return new TestClassLoader();
    }

    public static class TestClassLoader extends ClassLoader {
        private String targetClassName;
        private String targetMethodName;
        private CallbackHandler callbackHandler;
        private boolean trace;
        private boolean verify;

        public void setTargetClassName(String targetClassName) {
            this.targetClassName = targetClassName;
        }

        public void setCallbackHandler(CallbackHandler callbackHandler) {
            this.callbackHandler = callbackHandler;
        }

        public void setTrace(boolean trace) {
            this.trace = trace;
        }

        public void setVerify(boolean verify) {
            this.verify = verify;
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            if ((targetClassName == null || name.equals(targetClassName))) {
                try {
                    ClassNode classNode = ASMClassNodeLoader.get(JavaAssistUtils.javaNameToJvmName(name));

                    if (this.trace) {
                        System.out.println("## original #############################################################");
                        ASMClassWriter cw = new ASMClassWriter(pluginContext, classNode.name, classNode.superName, 0, null);
                        TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                        classNode.accept(tcv);
                    }

                    if (callbackHandler != null) {
                        callbackHandler.handle(classNode);
                    }

                     ASMClassWriter cw = new ASMClassWriter(pluginContext, classNode.name, classNode.superName, ClassWriter.COMPUTE_FRAMES, null);
                    if (this.trace) {
                        System.out.println("## modified #############################################################");
                        TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                        classNode.accept(tcv);
                    } else {
                        classNode.accept(cw);
                    }
                    byte[] bytecode = cw.toByteArray();
                    if (this.verify) {
                        CheckClassAdapter.verify(new ClassReader(bytecode), false, new PrintWriter(System.out));
                    }

                    return super.defineClass(name, bytecode, 0, bytecode.length);
                } catch (Exception ex) {
                    throw new ClassNotFoundException("Load error: " + ex.toString(), ex);
                }
            }
            return super.loadClass(name);
        }
    }

    public interface CallbackHandler {
        void handle(ClassNode classNode);
    }
}