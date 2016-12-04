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

public class ASMMethodNodeAdapterRenameTest {
    private ASMClassNodeLoader.TestClassLoader classLoader;

    @Before
    public void before() {
        this.classLoader = ASMClassNodeLoader.getClassLoader();
    }


    @Test
    public void rename() throws Exception {
        final String targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.NormalClass";
        classLoader.setTrace(false);
        classLoader.setTargetClassName(targetClassName);
        classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                List<MethodNode> methodNodes = classNode.methods;
                for (MethodNode methodNode : methodNodes) {
                    ASMMethodNodeAdapter adapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
                    if(!adapter.isConstructor()) {
                        adapter.rename(adapter.getName() + "_rename");
                    }
                }
            }
        });
        Class<?> clazz = classLoader.loadClass(targetClassName);
        Method method = clazz.getDeclaredMethod("sum_rename", int.class);
        method.invoke(clazz.newInstance(), 10);
   }
}

