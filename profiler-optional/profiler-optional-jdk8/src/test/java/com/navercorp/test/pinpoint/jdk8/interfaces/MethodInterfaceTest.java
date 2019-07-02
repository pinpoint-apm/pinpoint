/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.test.pinpoint.jdk8.interfaces;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.instrument.ASMClass;
import com.navercorp.pinpoint.profiler.instrument.ASMClassNodeAdapter;
import com.navercorp.pinpoint.profiler.instrument.ASMClassWriter;
import com.navercorp.pinpoint.profiler.instrument.ASMFieldNodeAdapter;
import com.navercorp.pinpoint.profiler.instrument.ASMMethodNodeAdapter;
import com.navercorp.pinpoint.profiler.instrument.EngineComponent;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author jaehong.kim
 */
public class MethodInterfaceTest {
    private final static InterceptorRegistryBinder interceptorRegistryBinder = new DefaultInterceptorRegistryBinder();
    private final static InstrumentContext pluginContext = mock(InstrumentContext.class);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public static void beforeClass() {
        interceptorRegistryBinder.bind();
    }

    @AfterClass
    public static void afterClass() {
        interceptorRegistryBinder.unbind();
    }

    @Test
    public void addInterceptor() throws Exception {
        final String targetInterfaceName = "com.navercorp.test.pinpoint.jdk8.interfaces.MethodInterface";
        final String targetClassName = "com.navercorp.test.pinpoint.jdk8.interfaces.MethodInterfaceClass";
        logger.debug("Add interceptor interface={}, class={}", targetInterfaceName, targetClassName);

        final int interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(new SimpleInterceptor());
        final InterceptorDefinition interceptorDefinition = new InterceptorDefinitionFactory().createInterceptorDefinition(SimpleInterceptor.class);
        final List<String> methodNameList = Arrays.asList("currentTimeMillis", "foo");
        TestClassLoader classLoader = new TestClassLoader();
        classLoader.addTargetClassName(targetClassName);
        classLoader.addTargetClassName(targetInterfaceName);
        classLoader.setCallbackHandler(new CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                List<MethodNode> methodNodes = classNode.methods;
                for (MethodNode methodNode : methodNodes) {
                    logger.debug("Handle class={}, method={}", classNode.name, methodNode.name);
                    if (methodNode.name.equals("<clinit>")) {
                        continue;
                    }

                    ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
                    if (methodNodeAdapter.isAbstract() || methodNodeAdapter.isNative()) {
                        continue;
                    }

                    if (!methodNameList.contains(methodNode.name)) {
                        continue;
                    }
                    methodNodeAdapter.addBeforeInterceptor(interceptorId, interceptorDefinition, 99);
                    logger.debug("Add before interceptor in method={}", methodNode.name);
                    methodNodeAdapter.addAfterInterceptor(interceptorId, interceptorDefinition, 99);
                    logger.debug("Add after interceptor in method={}", methodNode.name);
                }
            }
        });

        // static method
        Assert.assertFalse(SimpleInterceptor.before);
        logger.debug("Interface static method");
        Class<?> clazz = classLoader.loadClass(targetInterfaceName);
        Method method = clazz.getDeclaredMethod("currentTimeMillis");
        method.invoke(null);
        assertTrue(SimpleInterceptor.before);

        // reset
        SimpleInterceptor.before = false;

        // default method
        Assert.assertFalse(SimpleInterceptor.before);
        logger.debug("Interface default method");
        clazz = classLoader.loadClass(targetClassName);
        method = clazz.getDeclaredMethod("bar");
        method.invoke(clazz.newInstance());
        assertTrue(SimpleInterceptor.before);
    }

    @Test(expected = ClassFormatError.class)
    public void addField() throws Exception {
        final String targetInterfaceName = "com.navercorp.test.pinpoint.jdk8.interfaces.MethodInterface";
        final String targetClassName = "com.navercorp.test.pinpoint.jdk8.interfaces.MethodInterfaceClass";
        final String accessorClassName = "com.navercorp.test.pinpoint.jdk8.interfaces.SimpleAccessor";
        TestClassLoader classLoader = new TestClassLoader();
        classLoader.addTargetClassName(targetClassName);
        classLoader.addTargetClassName(targetInterfaceName);
        classLoader.setTrace(false);
        classLoader.setCallbackHandler(new CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                logger.debug("Add field class={}", classNode.name);
                ASMClassNodeAdapter classNodeAdapter = new ASMClassNodeAdapter(pluginContext, null, null, classNode);
                classNodeAdapter.addField("_$PINPOINT$_" + JavaAssistUtils.javaClassNameToVariableName(accessorClassName), Type.getDescriptor(int.class));
                classNodeAdapter.addInterface(accessorClassName);
                ASMFieldNodeAdapter fieldNodeAdapter = classNodeAdapter.getField("_$PINPOINT$_" + JavaAssistUtils.javaClassNameToVariableName(accessorClassName), null);
                classNodeAdapter.addGetterMethod("_$PINPOINT$_getTraceInt", fieldNodeAdapter);
                classNodeAdapter.addSetterMethod("_$PINPOINT$_setTraceInt", fieldNodeAdapter);
            }
        });
        logger.debug("Interface static method");
        Class<?> clazz = classLoader.loadClass(targetInterfaceName);
        Method method = clazz.getDeclaredMethod("currentTimeMillis");
        method.invoke(null);
    }

    @Test
    public void addMethod() throws Exception {
        final MethodNode methodNode = TestClassLoader.get("com.navercorp.test.pinpoint.jdk8.interfaces.SimpleClass", "welcome");
        final ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter("com/navercorp/test/pinpoint/jdk8/interfaces/SimpleClass", methodNode);

        final String targetInterfaceName = "com.navercorp.test.pinpoint.jdk8.interfaces.MethodInterface";
        TestClassLoader classLoader = new TestClassLoader();
        classLoader.addTargetClassName(targetInterfaceName);
        classLoader.setTrace(false);
        classLoader.setCallbackHandler(new CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                logger.debug("Add method class={}", classNode.name);
                ASMClassNodeAdapter classNodeAdapter = new ASMClassNodeAdapter(pluginContext, null, null, classNode);
                classNodeAdapter.copyMethod(methodNodeAdapter);
            }
        });
        logger.debug("Interface static method");
        Class<?> clazz = classLoader.loadClass(targetInterfaceName);
        Method method = clazz.getDeclaredMethod("welcome");
        method.invoke(null);
    }

    @Test
    public void isInterceptable() throws Exception {
        ClassNode classNode = TestClassLoader.get("com.navercorp.test.pinpoint.jdk8.interfaces.MethodInterface");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        EngineComponent engineComponent = mock(EngineComponent.class);
        ASMClass clazz = new ASMClass(engineComponent, pluginContext, classLoader, null, classNode);
        assertTrue(clazz.isInterceptable());
    }

    public static class TestClassLoader extends ClassLoader {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private List<String> targetClassNameList = new ArrayList<>();
        private CallbackHandler callbackHandler;
        private boolean trace;
        private boolean verify;

        public void addTargetClassName(String targetClassName) {
            this.targetClassNameList.add(targetClassName);
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

        // only use for test.
        public static ClassNode get(final String className) throws Exception {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassReader cr = new ClassReader(classLoader.getResourceAsStream(JavaAssistUtils.javaClassNameToJvmResourceName(className)));
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

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            logger.debug("TestClassLoader.loadClass " + name);
            if (targetClassNameList.contains(name)) {
                try {
                    ClassNode classNode = get(JavaAssistUtils.javaNameToJvmName(name));
                    final int majorVersion = classNode.version & 0xFFFF;
                    logger.debug("Version {}, {}", classNode.version, majorVersion);
                    //classNode.version = 50;
                    if (this.trace) {
                        logger.debug("Trace original #############################################################");
                        ASMClassWriter cw = new ASMClassWriter(pluginContext, 0, null);
                        TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                        classNode.accept(tcv);
                    }

                    if (callbackHandler != null) {
                        callbackHandler.handle(classNode);
                    }

                    ASMClassWriter cw = new ASMClassWriter(pluginContext, ClassWriter.COMPUTE_FRAMES, null);
                    if (this.trace) {
                        logger.debug("## Trace modified #############################################################");
                        TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                        classNode.accept(tcv);
                    } else {
                        classNode.accept(cw);
                    }
                    byte[] bytecode = cw.toByteArray();
                    if (this.verify) {
                        CheckClassAdapter.verify(new ClassReader(bytecode), false, new PrintWriter(System.out));
                    }
                    logger.debug("TestClassLoader.defineClass name={}, length={}", name, bytecode.length);
                    return super.defineClass(name, bytecode, 0, bytecode.length);
                } catch (Exception ex) {
                    throw new ClassNotFoundException("Load error: " + ex.toString(), ex);
                }
            }
            logger.debug("Super.loadClass " + name);
            return super.loadClass(name);
        }
    }

    public interface CallbackHandler {
        void handle(ClassNode classNode);
    }
}