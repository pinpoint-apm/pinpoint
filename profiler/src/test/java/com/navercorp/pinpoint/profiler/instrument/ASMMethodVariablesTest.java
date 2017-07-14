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

import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinition;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.instrument.mock.ApiIdAwareInterceptor;
import com.navercorp.pinpoint.profiler.instrument.mock.ArgsArrayInterceptor;
import com.navercorp.pinpoint.profiler.instrument.mock.BasicInterceptor;
import com.navercorp.pinpoint.profiler.instrument.mock.StaticInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.registry.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author
 */
public class ASMMethodVariablesTest {

    @Test
    public void getParameterTypes() throws Exception {
        final ClassNode classNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
            String[] parameterTypes = methodNodeAdapter.getParameterTypes();
            Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
            assertEquals(parameterTypes.length, argumentTypes.length);
        }
    }

    @Test
    public void getParameterNames() throws Exception {
        final ClassNode classNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
            String[] types = methodNodeAdapter.getParameterTypes();
            String[] names = methodNodeAdapter.getParameterNames();
            assertEquals(methodNode.name, types.length, names.length);
        }
    }

    @Test
    public void getReturnType() throws Exception {
        final ClassNode classNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ReturnClass");
        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
            assertNotNull(methodNodeAdapter.getReturnType());
        }
    }

    @Test
    public void hasInterceptor() throws Exception {
        InterceptorRegistryBinder interceptorRegistryBinder = new DefaultInterceptorRegistryBinder();
        int interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(new ArgsArrayInterceptor());
        final InterceptorDefinition interceptorDefinition = new InterceptorDefinitionFactory().createInterceptorDefinition(ArgsArrayInterceptor.class);

        final ClassNode classNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter(classNode.name, methodNode);
            assertEquals(false, methodNodeAdapter.hasInterceptor());
            methodNodeAdapter.addBeforeInterceptor(interceptorId, interceptorDefinition, -1);
            assertEquals(true, methodNodeAdapter.hasInterceptor());
        }
    }

    @Test
    public void initInterceptorLocalVariables() throws Exception {
        MethodNode methodNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ConstructorChildClass", "<init>");
        ASMMethodVariables variables = new ASMMethodVariables("com/navercorp/pinpoint/profiler/instrument/mock/ConstructorChildClass", methodNode);

        assertNull(variables.getEnterInsnNode());
        assertNull(variables.getEnterInsnNode());

        InterceptorRegistryBinder interceptorRegistryBinder = new DefaultInterceptorRegistryBinder();
        int interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(new ArgsArrayInterceptor());
        final InterceptorDefinition interceptorDefinition = new InterceptorDefinitionFactory().createInterceptorDefinition(ArgsArrayInterceptor.class);

        InsnList instructions = new InsnList();
        boolean first = variables.initInterceptorLocalVariables(instructions, interceptorId, interceptorDefinition, -1);
        assertEquals(true, first);
        assertNotNull(variables.getEnterInsnNode());
        assertNotNull(variables.getEnterInsnNode());
    }

    @Test
    public void findInitConstructorInstruction() throws Exception {
        MethodNode methodNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.AbstractClass", "<init>");
        ASMMethodVariables variables = new ASMMethodVariables("com/navercorp/pinpoint/profiler/instrument/mock/AbstractClass", methodNode);
        AbstractInsnNode instruction = variables.findInitConstructorInstruction();
        assertEquals(7, methodNode.instructions.indexOf(instruction));
    }

    @Test
    public void getInterceptorParameterCount() throws Exception {
        MethodNode methodNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ConstructorChildClass", "<init>");
        ASMMethodVariables variables = new ASMMethodVariables("com/navercorp/pinpoint/profiler/instrument/mock/ConstructorChildClass", methodNode);

        assertEquals(1, variables.getInterceptorParameterCount(new InterceptorDefinitionFactory().createInterceptorDefinition(ArgsArrayInterceptor.class)));
        assertEquals(4, variables.getInterceptorParameterCount(new InterceptorDefinitionFactory().createInterceptorDefinition(StaticInterceptor.class)));
        assertEquals(2, variables.getInterceptorParameterCount(new InterceptorDefinitionFactory().createInterceptorDefinition(ApiIdAwareInterceptor.class)));
        assertEquals(5, variables.getInterceptorParameterCount(new InterceptorDefinitionFactory().createInterceptorDefinition(BasicInterceptor.class)));
    }

    @Test
    public void initLocalVariables() throws Exception {
        final String className = "com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass";
        final MethodNode methodNode = ASMClassNodeLoader.get(className, "argByteType");
        String[] exceptions = null;
        if (methodNode.exceptions != null) {
            exceptions = methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]);
        }

        final ASMMethodNodeAdapter methodNodeAdapter = new ASMMethodNodeAdapter("foo", new MethodNode(methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, exceptions));
        ASMMethodVariables variables = new ASMMethodVariables(JavaAssistUtils.javaNameToJvmName(className), methodNodeAdapter.getMethodNode());
        assertEquals(0, variables.getLocalVariables().size());

        InsnList instructions = new InsnList();
        variables.initLocalVariables(instructions);

        assertEquals(2, variables.getLocalVariables().size());
        assertEquals("this", variables.getLocalVariables().get(0).name);
        assertEquals("Lcom/navercorp/pinpoint/profiler/instrument/mock/ArgsClass;", variables.getLocalVariables().get(0).desc);
        assertEquals("byte", variables.getLocalVariables().get(1).name);
        assertEquals("B", variables.getLocalVariables().get(1).desc);
    }
}