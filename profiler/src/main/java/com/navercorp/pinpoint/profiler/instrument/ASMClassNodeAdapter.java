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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMClassNodeAdapter {

    public static ASMClassNodeAdapter get(final InstrumentContext pluginContext, final ClassLoader classLoader, final String classInternalName) {
        return get(pluginContext, classLoader, classInternalName, false);
    }

    public static ASMClassNodeAdapter get(final InstrumentContext pluginContext, final ClassLoader classLoader, final String classInternalName, final boolean skipCode) {
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        if (classInternalName == null) {
            throw new NullPointerException("classInternalName must not be null");
        }

        InputStream in = null;
        try {
            in = pluginContext.getResourceAsStream(classLoader, classInternalName + ".class");
            if (in != null) {
                final ClassReader classReader = new ClassReader(in);
                final ClassNode classNode = new ClassNode();
                if (skipCode) {
                    classReader.accept(classNode, ClassReader.SKIP_CODE);
                } else {
                    classReader.accept(classNode, 0);
                }

                return new ASMClassNodeAdapter(pluginContext, classLoader, classNode, skipCode);
            }
        } catch (IOException ignored) {
            // not found class.
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        return null;
    }

    private final InstrumentContext pluginContext;
    private final ClassLoader classLoader;
    private final ClassNode classNode;
    private final boolean skipCode;

    public ASMClassNodeAdapter(final InstrumentContext pluginContext, final ClassLoader classLoader, final ClassNode classNode) {
        this(pluginContext, classLoader, classNode, false);
    }

    public ASMClassNodeAdapter(final InstrumentContext pluginContext, final ClassLoader classLoader, final ClassNode classNode, final boolean skipCode) {
        this.pluginContext = pluginContext;
        this.classLoader = classLoader;
        this.classNode = classNode;
        this.skipCode = skipCode;
    }

    public String getInternalName() {
        return this.classNode.name;
    }

    public String getName() {
        return this.classNode.name == null ? null : JavaAssistUtils.jvmNameToJavaName(this.classNode.name);
    }

    public String getSuperClassInternalName() {
        return this.classNode.superName;
    }

    public String getSuperClassName() {
        return this.classNode.superName == null ? null : JavaAssistUtils.jvmNameToJavaName(this.classNode.superName);
    }

    public boolean isInterface() {
        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

    public boolean isAnnotation() {
        return (classNode.access & Opcodes.ACC_ANNOTATION) != 0;
    }

    public String[] getInterfaceNames() {
        final List<String> interfaces = this.classNode.interfaces;
        if (interfaces == null || interfaces.size() == 0) {
            return new String[0];
        }

        final List<String> list = new ArrayList<String>();
        for (String name : interfaces) {
            if (name != null) {
                list.add(JavaAssistUtils.jvmNameToJavaName(name));
            }
        }

        return list.toArray(new String[list.size()]);
    }

    public ASMMethodNodeAdapter getDeclaredMethod(final String methodName, final String desc) {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        return findDeclaredMethod(methodName, desc);
    }

    public boolean hasDeclaredMethod(final String methodName, final String desc) {
        return findDeclaredMethod(methodName, desc) != null;
    }

    private ASMMethodNodeAdapter findDeclaredMethod(final String methodName, final String desc) {
        if (methodName == null) {
            return null;
        }

        final List<MethodNode> declaredMethods = classNode.methods;
        if (declaredMethods == null) {
            return null;
        }

        for (MethodNode methodNode : declaredMethods) {
            if (methodNode.name == null || !methodNode.name.equals(methodName)) {
                continue;
            }

            if (desc == null || (methodNode.desc != null && methodNode.desc.startsWith(desc))) {
                return new ASMMethodNodeAdapter(getInternalName(), methodNode);
            }
        }

        return null;
    }

    public List<ASMMethodNodeAdapter> getDeclaredMethods() {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<ASMMethodNodeAdapter>();
        if (this.classNode.methods == null) {
            return methodNodes;
        }

        for (MethodNode methodNode : this.classNode.methods) {
            if (methodNode.name == null || methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) {
                // skip constructor(<init>) and static initializer block(<clinit>)
                continue;
            }
            methodNodes.add(new ASMMethodNodeAdapter(getInternalName(), methodNode));
        }

        return methodNodes;
    }

    public boolean hasOutClass(final String methodName, final String desc) {
        if (methodName == null || this.classNode.outerClass == null || this.classNode.outerMethod == null || !this.classNode.outerMethod.equals(methodName)) {
            return false;
        }

        if (desc == null || (this.classNode.outerMethodDesc != null && this.classNode.outerMethodDesc.startsWith(desc))) {
            return true;
        }

        return false;
    }

    public boolean hasMethod(final String methodName, final String desc) {
        if (hasDeclaredMethod(methodName, desc)) {
            return true;
        }

        if (this.classNode.superName != null) {
            // skip code.
            final ASMClassNodeAdapter classNode = ASMClassNodeAdapter.get(this.pluginContext, this.classLoader, this.classNode.superName, true);
            if (classNode != null) {
                return classNode.hasMethod(methodName, desc);
            }
        }

        return false;
    }

    public ASMFieldNodeAdapter getField(final String fieldName, final String fieldDesc) {
        if (fieldName == null || this.classNode.fields == null) {
            return null;
        }

        final List<FieldNode> fields = this.classNode.fields;
        for (FieldNode fieldNode : fields) {
            if ((fieldNode.name != null && fieldNode.name.equals(fieldName)) && (fieldDesc == null || (fieldNode.desc != null && fieldNode.desc.equals(fieldDesc)))) {
                return new ASMFieldNodeAdapter(fieldNode);
            }
        }

        // find interface.
        final List<String> interfaces = this.classNode.interfaces;
        if (interfaces != null && interfaces.size() > 0) {
            for (String interfaceClassName : interfaces) {
                if (interfaceClassName == null) {
                    continue;
                }

                final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter.get(this.pluginContext, this.classLoader, interfaceClassName, true);
                if (classNodeAdapter != null) {
                    final ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField(fieldName, fieldDesc);
                    if (fieldNode != null) {
                        return fieldNode;
                    }
                }
            }
        }

        // find super class.
        if (this.classNode.superName != null) {
            final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter.get(this.pluginContext, this.classLoader, this.classNode.superName, true);
            if (classNodeAdapter != null) {
                final ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField(fieldName, fieldDesc);
                if (fieldNode != null) {
                    return fieldNode;
                }
            }
        }

        return null;
    }

    public ASMFieldNodeAdapter addField(final String fieldName, final Class<?> fieldClass) {
        if (fieldName == null || fieldClass == null) {
            throw new IllegalArgumentException("fieldNode name or fieldNode class must not be null.");
        }

        final Type type = Type.getType(fieldClass);
        final FieldNode fieldNode = new FieldNode(Opcodes.ACC_PRIVATE, fieldName, type.getDescriptor(), null, null);
        if (this.classNode.fields == null) {
            this.classNode.fields = new ArrayList<FieldNode>();
        }
        this.classNode.fields.add(fieldNode);

        return new ASMFieldNodeAdapter(fieldNode);
    }

    public ASMMethodNodeAdapter addDelegatorMethod(final ASMMethodNodeAdapter superMethodNode) {
        if (superMethodNode == null) {
            throw new IllegalArgumentException("super method annotation must not be null.");
        }

        String[] exceptions = null;
        if (superMethodNode.getExceptions() != null) {
            exceptions = superMethodNode.getExceptions().toArray(new String[superMethodNode.getExceptions().size()]);
        }

        final ASMMethodNodeAdapter methodNode = new ASMMethodNodeAdapter(getInternalName(), new MethodNode(superMethodNode.getAccess(), superMethodNode.getName(), superMethodNode.getDesc(), superMethodNode.getSignature(), exceptions));
        methodNode.addDelegator(superMethodNode.getDeclaringClassInternalName());
        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<MethodNode>();
        }
        this.classNode.methods.add(methodNode.getMethodNode());

        return methodNode;
    }

    public void addGetterMethod(final String methodName, final ASMFieldNodeAdapter fieldNode) {
        if (methodName == null || fieldNode == null) {
            throw new IllegalArgumentException("method name or fieldNode annotation must not be null.");
        }

        // no argument is ().
        final String desc = "()" + fieldNode.getDesc();
        final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        if (methodNode.instructions == null) {
            methodNode.instructions = new InsnList();
        }
        final InsnList instructions = methodNode.instructions;
        // load this.
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        // get fieldNode.
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldNode.getName(), fieldNode.getDesc()));
        // return of type.
        final Type type = Type.getType(fieldNode.getDesc());
        instructions.add(new InsnNode(type.getOpcode(Opcodes.IRETURN)));

        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<MethodNode>();
        }
        this.classNode.methods.add(methodNode);
    }

    public void addSetterMethod(final String methodName, final ASMFieldNodeAdapter fieldNode) {
        if (methodName == null || fieldNode == null) {
            throw new IllegalArgumentException("method name or fieldNode annotation must not be null.");
        }

        // void is V.
        final String desc = "(" + fieldNode.getDesc() + ")V";
        final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        if (methodNode.instructions == null) {
            methodNode.instructions = new InsnList();
        }
        final InsnList instructions = methodNode.instructions;
        // load this.
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        final Type type = Type.getType(fieldNode.getDesc());
        // put field.
        instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), 1));
        instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, fieldNode.getName(), fieldNode.getDesc()));
        // return.
        instructions.add(new InsnNode(Opcodes.RETURN));

        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<MethodNode>();
        }
        this.classNode.methods.add(methodNode);
    }

    public void addInterface(final String interfaceName) {
        if (interfaceName == null) {
            throw new IllegalArgumentException("interface name must not be null.");
        }

        if (this.classNode.interfaces == null) {
            this.classNode.interfaces = new ArrayList<String>();
        }
        this.classNode.interfaces.add(JavaAssistUtils.javaNameToJvmName(interfaceName));
    }

    public void copyMethod(final ASMMethodNodeAdapter methodNode) {
        if (methodNode == null) {
            throw new IllegalArgumentException("method annotation must not be null");
        }

        // change local call.
        final ASMMethodInsnNodeRemapper remapper = new ASMMethodInsnNodeRemapper();
        remapper.addFilter(methodNode.getDeclaringClassInternalName(), null, null);
        remapper.setOwner(this.classNode.name);
        // remap method call.
        methodNode.remapMethodInsnNode(remapper);
        // remap desc of this.
        methodNode.remapLocalVariables("this", Type.getObjectType(this.classNode.name).getDescriptor());

        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<MethodNode>();
        }
        this.classNode.methods.add(methodNode.getMethodNode());
    }

    public boolean hasAnnotation(final Class<?> annotationClass) {
        if (annotationClass == null) {
            return false;
        }

        final String desc = Type.getDescriptor(annotationClass);
        return hasAnnotation(desc, this.classNode.invisibleAnnotations) || hasAnnotation(desc, this.classNode.visibleAnnotations);
    }

    private boolean hasAnnotation(final String annotationClassDesc, final List<AnnotationNode> annotationNodes) {
        if (annotationClassDesc == null || annotationNodes == null) {
            return false;
        }

        for (AnnotationNode annotation : annotationNodes) {
            if (annotation.desc != null && annotation.desc.equals(annotationClassDesc)) {
                return true;
            }
        }

        return false;
    }

    public boolean subclassOf(final String classInternalName) {
        if (classInternalName == null) {
            return false;
        }

        if (classInternalName.equals("java/lang/Object")) {
            // super is root.
            return true;
        }

        ASMClassNodeAdapter classNode = this;
        while (classNode != null) {
            if (classInternalName.equals(classNode.getInternalName())) {
                return true;
            }

            final String superClassInternalName = classNode.getSuperClassInternalName();
            if (superClassInternalName == null || superClassInternalName.equals("java/lang/Object")) {
                // find root annotation.
                return false;
            }

            // skip code.
            classNode = ASMClassNodeAdapter.get(this.pluginContext, this.classLoader, superClassInternalName, true);
        }

        return false;
    }

    public List<ASMClassNodeAdapter> getInnerClasses() {
        if (this.classNode.innerClasses == null) {
            return Collections.EMPTY_LIST;
        }

        final List<ASMClassNodeAdapter> innerClasses = new ArrayList<ASMClassNodeAdapter>();
        final List<InnerClassNode> innerClassNodes = this.classNode.innerClasses;
        for (InnerClassNode node : innerClassNodes) {
            if (node.name == null) {
                continue;
            }
            // skip code.
            ASMClassNodeAdapter adapter = get(this.pluginContext, this.classLoader, node.name, true);
            if (adapter != null) {
                innerClasses.add(adapter);
            }
        }

        return innerClasses;
    }

    public byte[] toByteArray() {
        final int majorVersion = this.classNode.version & 0xFFFF;
        int flags = ClassWriter.COMPUTE_FRAMES;
        if (majorVersion <= 49) {
            // java 1.5 and less.
            flags = ClassWriter.COMPUTE_MAXS;
        }

        final ClassWriter classWriter = new ASMClassWriter(this.pluginContext, flags, this.classLoader);
        this.classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
