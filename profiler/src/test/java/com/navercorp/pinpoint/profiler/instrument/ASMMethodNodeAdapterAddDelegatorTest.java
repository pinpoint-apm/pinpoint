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

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMMethodNodeAdapterAddDelegatorTest {
    private ASMClassNodeLoader.TestClassLoader classLoader;

    @Before
    public void before() {
        this.classLoader = ASMClassNodeLoader.getClassLoader();
    }

    @Test
    public void addDelegatorMethod() throws Exception {
        Class clazz = addDelegatorMethod("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass", "com.navercorp.pinpoint.profiler.instrument.mock.BaseClass", "base");
        Method method = clazz.getDeclaredMethod("base");
        method.invoke(clazz.newInstance());
    }

    @Test
    public void addDelegatorStaticMethod() throws Exception {
        Class clazz = addDelegatorMethod("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass", "com.navercorp.pinpoint.profiler.instrument.mock.BaseClass", "getInstance");
        Method method = clazz.getDeclaredMethod("getInstance");
        method.invoke(clazz.newInstance());
    }

    private Class addDelegatorMethod(final String targetClassName, final String superClassName, final String methodName) throws Exception {
        final ClassNode superClassNode = ASMClassNodeLoader.get(superClassName);
        List<MethodNode> methodNodes = superClassNode.methods;
        final MethodNode methodNode = findMethodNode(methodName, methodNodes);

        classLoader.setTargetClassName(targetClassName);
        classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(final ClassNode classNode) {
                String[] exceptions = null;
                if (methodNode.exceptions != null) {
                    exceptions = methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]);
                }

                final MethodNode newMethodNode = new MethodNode(methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, exceptions);
                final ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(targetClassName, newMethodNode);
                methodNodeAdapter.addDelegator(superClassName.replace('.', '/'));
                classNode.methods.add(newMethodNode);
            }
        });
        return classLoader.loadClass(targetClassName);
    }

    private MethodNode findMethodNode(final String methodName, final List<MethodNode> methodNodes) {
        for (MethodNode methodNode : methodNodes) {
            if (methodNode.name.equals(methodName)) {
                return methodNode;
            }
        }

        return null;
    }
}