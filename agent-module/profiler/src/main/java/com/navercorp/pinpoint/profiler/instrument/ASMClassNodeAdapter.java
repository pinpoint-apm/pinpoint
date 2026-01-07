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

import com.navercorp.pinpoint.bootstrap.instrument.ClassInputStreamProvider;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.instrument.scanner.ClassScannerFactory;
import com.navercorp.pinpoint.profiler.instrument.scanner.Scanner;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.profiler.util.StringMatchUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ASMClassNodeAdapter {
    private static final String RECORD_CLASS_SUPER_NAME = "java/lang/Record";

    private static final Logger logger = LogManager.getLogger(ASMClassNodeAdapter.class);

    public static ASMClassNodeAdapter get(final ClassInputStreamProvider pluginClassInputStreamProvider, final ClassLoader classLoader, ProtectionDomain protectionDomain, final String classInternalName) {
        return get(pluginClassInputStreamProvider, classLoader, protectionDomain, classInternalName, false);
    }

    public static ASMClassNodeAdapter get(final ClassInputStreamProvider pluginClassInputStreamProvider, final ClassLoader classLoader, ProtectionDomain protectionDomain, final String classInternalName, final boolean skipCode) {
        Objects.requireNonNull(pluginClassInputStreamProvider, "pluginInputStreamProvider");
        Objects.requireNonNull(classInternalName, "classInternalName");

        final String classPath = classInternalName.concat(".class");
        final byte[] bytes = readStream(classPath, pluginClassInputStreamProvider, protectionDomain, classLoader);
        if (bytes == null) {
            return null;
        }
        final ClassReader classReader = new ClassReader(bytes);
        final ClassNode classNode = new ClassNode();

        final int parsingOptions = getParsingOption(skipCode);
        classReader.accept(classNode, parsingOptions);

        return new ASMClassNodeAdapter(pluginClassInputStreamProvider, classLoader, protectionDomain, classNode, skipCode);
    }

    private static int getParsingOption(boolean skipCode) {
        if (skipCode) {
            return ClassReader.SKIP_CODE;
        } else {
            return 0;
        }
    }

    private static byte[] readStream(String classPath, ClassInputStreamProvider pluginClassInputStreamProvider, ProtectionDomain protectionDomain, ClassLoader classLoader) {

        final Scanner scanner = ClassScannerFactory.newScanner(protectionDomain);
        if (scanner != null) {
            try {
                final InputStream stream = scanner.openStream(classPath);
                if (stream != null) {
                    try {
                        return IOUtils.toByteArray(stream);
                    } catch (IOException e) {
                        logger.warn("bytecode read fail scanner:{} path:{}", scanner, classPath);
                        return null;
                    }
                }
            } finally {
                scanner.close();
            }
        }

        final InputStream in = pluginClassInputStreamProvider.getResourceAsStream(classLoader, classPath);
        if (in != null) {
            try {
                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                logger.warn("bytecode read fail path:{}", classPath);
                return null;
            }
        }
        return null;
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final ClassInputStreamProvider pluginInputStreamProvider;
    private final ClassLoader classLoader;
    private final ProtectionDomain protectionDomain;
    private final ClassNode classNode;
    private final boolean skipCode;

    public ASMClassNodeAdapter(final ClassInputStreamProvider pluginInputStreamProvider, final ClassLoader classLoader, ProtectionDomain protectionDomain, final ClassNode classNode) {
        this(pluginInputStreamProvider, classLoader, protectionDomain, classNode, false);
    }

    public ASMClassNodeAdapter(final ClassInputStreamProvider pluginInputStreamProvider, final ClassLoader classLoader, ProtectionDomain protectionDomain, final ClassNode classNode, final boolean skipCode) {
        this.pluginInputStreamProvider = pluginInputStreamProvider;
        this.classLoader = classLoader;
        this.protectionDomain = protectionDomain;
        this.classNode = classNode;
        this.skipCode = skipCode;
    }

    public String getInternalName() {
        return this.classNode.name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
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
        if (CollectionUtils.isEmpty(interfaces)) {
            return EMPTY_STRING_ARRAY;
        }

        final List<String> list = new ArrayList<>(interfaces.size());
        for (String name : interfaces) {
            if (name != null) {
                list.add(JavaAssistUtils.jvmNameToJavaName(name));
            }
        }

        return list.toArray(EMPTY_STRING_ARRAY);
    }

    public ASMMethodNodeAdapter getDeclaredMethod(final String methodName, final String desc) {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        return findDeclaredMethod(methodName, desc);
    }

    public List<ASMMethodNodeAdapter> getDeclaredConstructors() {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        return findDeclaredMethod("<init>");
    }

    public boolean hasDeclaredMethod(final String methodName, final String desc) {
        return findDeclaredMethod(methodName, desc) != null;
    }

    private ASMMethodNodeAdapter findDeclaredMethod(final String methodName, final String desc) {
        Objects.requireNonNull(methodName, "methodName");

        final List<MethodNode> declaredMethods = classNode.methods;
        if (CollectionUtils.isEmpty(declaredMethods)) {
            return null;
        }

        for (MethodNode methodNode : declaredMethods) {
            if (!StringMatchUtils.equals(methodNode.name, methodName)) {
                continue;
            }

            if (desc == null || StringMatchUtils.startWith(methodNode.desc, desc)) {
                return new ASMMethodNodeAdapter(getInternalName(), methodNode);
            }
        }

        return null;
    }

    private List<ASMMethodNodeAdapter> findDeclaredMethod(final String methodName) {
        Objects.requireNonNull(methodName, "methodName");

        final List<MethodNode> declaredMethods = classNode.methods;
        if (CollectionUtils.isEmpty(declaredMethods)) {
            return Collections.emptyList();
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<>();
        for (MethodNode methodNode : declaredMethods) {
            if (!StringMatchUtils.equals(methodNode.name, methodName)) {
                continue;
            }

            methodNodes.add(new ASMMethodNodeAdapter(getInternalName(), methodNode));
        }
        return methodNodes;
    }


    public List<ASMMethodNodeAdapter> getDeclaredMethods() {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        final List<MethodNode> methods = this.classNode.methods;
        if (CollectionUtils.isEmpty(methods)) {
            return Collections.emptyList();
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<>(methods.size());
        for (MethodNode methodNode : methods) {
            final String methodName = methodNode.name;
            if (methodName == null || methodName.equals("<init>") || methodName.equals("<clinit>")) {
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

        if (desc == null) {
            return true;
        }
        return StringMatchUtils.startWith(this.classNode.outerMethodDesc, desc);
    }

    public boolean hasMethod(final String methodName, final String desc) {
        if (hasDeclaredMethod(methodName, desc)) {
            return true;
        }

        final String superName = this.classNode.superName;
        if (superName != null) {
            // skip code.
            final ASMClassNodeAdapter classNode = ASMClassNodeAdapter.get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, superName, true);
            if (classNode != null) {
                return classNode.hasMethod(methodName, desc);
            }
        }

        return false;
    }

    public ASMFieldNodeAdapter getField(final String fieldName, final String fieldDesc) {
        Objects.requireNonNull(fieldName, "fieldName");

        final List<FieldNode> fields = this.classNode.fields;
        if (fields == null) {
            return null;
        }

        for (FieldNode fieldNode : fields) {
            if (StringMatchUtils.equals(fieldNode.name, fieldName) && (fieldDesc == null || (StringMatchUtils.equals(fieldNode.desc, fieldDesc)))) {
                return new ASMFieldNodeAdapter(fieldNode);
            }
        }


        // find interface.
        final List<String> interfaces = this.classNode.interfaces;
        if (CollectionUtils.hasLength(interfaces)) {
            for (String interfaceClassName : interfaces) {
                if (interfaceClassName == null) {
                    continue;
                }

                final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter.get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, interfaceClassName, true);
                if (classNodeAdapter != null) {
                    final ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField(fieldName, fieldDesc);
                    if (fieldNode != null) {
                        return fieldNode;
                    }
                }
            }
        }

        // find super class.
        final String superName = this.classNode.superName;
        if (superName != null) {
            final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter.get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, superName, true);
            if (classNodeAdapter != null) {
                final ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField(fieldName, fieldDesc);
                if (fieldNode != null) {
                    return fieldNode;
                }
            }
        }

        return null;
    }

    public ASMFieldNodeAdapter addField(final String fieldName, final String fieldDesc) {
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(fieldDesc, "fieldDesc");
        final FieldNode fieldNode = new FieldNode(getFieldAccessFlags(), fieldName, fieldDesc, null, null);
        addFieldNode0(fieldNode);

        return new ASMFieldNodeAdapter(fieldNode);
    }

    private int getFieldAccessFlags() {
        // Field added by pinpoint must not be serialized
        return Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT;
    }

    private void addFieldNode0(FieldNode fieldNode) {
        if (this.classNode.fields == null) {
            this.classNode.fields = new ArrayList<>();
        }
        this.classNode.fields.add(fieldNode);
    }

    public ASMMethodNodeAdapter addDelegatorMethod(final ASMMethodNodeAdapter superMethodNode) {
        Objects.requireNonNull(superMethodNode, "superMethodNode");

        final String[] exceptions = getSuperMethodExceptions(superMethodNode);

        final MethodNode rawMethodNode = new MethodNode(superMethodNode.getAccess(), superMethodNode.getName(), superMethodNode.getDesc(), superMethodNode.getSignature(), exceptions);
        final ASMMethodNodeAdapter methodNode = new ASMMethodNodeAdapter(getInternalName(), rawMethodNode);
        methodNode.addDelegator(superMethodNode.getDeclaringClassInternalName());
        addMethodNode0(methodNode.getMethodNode());

        return methodNode;
    }

    private String[] getSuperMethodExceptions(ASMMethodNodeAdapter superMethodNode) {
        final List<String> superMethodNodeExceptions = superMethodNode.getExceptions();
        if (superMethodNodeExceptions == null) {
            return null;
        }
        return superMethodNodeExceptions.toArray(EMPTY_STRING_ARRAY);
    }

    public void addGetterMethod(final String methodName, final ASMFieldNodeAdapter fieldNode) {
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(fieldNode, "fieldNode");


        // no argument is ().
        final String desc = "()" + fieldNode.getDesc();
        final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        final InsnList instructions = getInsnList(methodNode);
        // load this.
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        // get fieldNode.
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldNode.getName(), fieldNode.getDesc()));
        // return of type.
        final Type type = fieldNode.getJavaType();
        instructions.add(new InsnNode(type.getOpcode(Opcodes.IRETURN)));

        addMethodNode0(methodNode);
    }

    private void addMethodNode0(MethodNode methodNode) {
        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<>();
        }
        this.classNode.methods.add(methodNode);
    }

    public void addSetterMethod(final String methodName, final ASMFieldNodeAdapter fieldNode) {
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(fieldNode, "fieldNode");


        // void is V.
        final String desc = "(" + fieldNode.getDesc() + ")V";
        final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        final InsnList instructions = getInsnList(methodNode);
        // load this.
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        final Type type = fieldNode.getJavaType();
        // put field.
        instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), 1));
        instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, fieldNode.getName(), fieldNode.getDesc()));
        // return.
        instructions.add(new InsnNode(Opcodes.RETURN));

        addMethodNode0(methodNode);
    }

    private InsnList getInsnList(MethodNode methodNode) {
        if (methodNode.instructions == null) {
            methodNode.instructions = new InsnList();
        }
        return methodNode.instructions;
    }

    public void addInterface(final String interfaceName) {
        Objects.requireNonNull(interfaceName, "interfaceName");

        if (this.classNode.interfaces == null) {
            this.classNode.interfaces = new ArrayList<>();
        }
        this.classNode.interfaces.add(JavaAssistUtils.javaNameToJvmName(interfaceName));
    }

    public void copyMethod(final ASMMethodNodeAdapter methodNode) {
        Objects.requireNonNull(methodNode, "methodNode");

        // change local call.
        final ASMMethodInsnNodeRemapper.Builder remapBuilder = new ASMMethodInsnNodeRemapper.Builder();
        remapBuilder.addFilter(methodNode.getDeclaringClassInternalName(), null, null);
        remapBuilder.setOwner(this.classNode.name);
        // remap method call.
        final ASMMethodInsnNodeRemapper remapper = remapBuilder.build();
        methodNode.remapMethodInsnNode(remapper);
        // remap desc of this.
        methodNode.remapLocalVariables("this", Type.getObjectType(this.classNode.name).getDescriptor());

        addMethodNode0(methodNode.getMethodNode());
    }

    public boolean hasAnnotation(final Class<?> annotationClass) {
        if (annotationClass == null) {
            return false;
        }

        final String desc = Type.getDescriptor(annotationClass);
        return hasAnnotation(desc, this.classNode.invisibleAnnotations) || hasAnnotation(desc, this.classNode.visibleAnnotations);
    }

    private boolean hasAnnotation(final String annotationClassDesc, final List<AnnotationNode> annotationNodes) {
        if (annotationClassDesc == null) {
            return false;
        }
        if (annotationNodes == null) {
            return false;
        }

        for (AnnotationNode annotation : annotationNodes) {
            if (StringMatchUtils.equals(annotation.desc, annotationClassDesc)) {
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
            classNode = ASMClassNodeAdapter.get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, superClassInternalName, true);
        }

        return false;
    }

    public List<ASMClassNodeAdapter> getInnerClasses() {
        if (this.classNode.innerClasses == null) {
            return Collections.emptyList();
        }

        final List<ASMClassNodeAdapter> innerClasses = new ArrayList<>();
        final List<InnerClassNode> innerClassNodes = this.classNode.innerClasses;
        for (InnerClassNode node : innerClassNodes) {
            if (node.name == null) {
                continue;
            }
            // skip code.
            ASMClassNodeAdapter adapter = get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, node.name, true);
            if (adapter != null) {
                innerClasses.add(adapter);
            }
        }

        return innerClasses;
    }

    public boolean isRecord() {
        return RECORD_CLASS_SUPER_NAME.equals(classNode.superName);
    }

    public int getMajorVersion() {
        final int majorVersion =  this.classNode.version & 0xFFFF;
        return majorVersion;
    }

    public byte[] toByteArray() {
        final int majorVersion = this.classNode.version & 0xFFFF;
        int flags = ClassWriter.COMPUTE_FRAMES;
        if (majorVersion <= 49) {
            // java 1.5 and less.
            flags = ClassWriter.COMPUTE_MAXS;
        }

        final ClassWriter classWriter = new ASMClassWriter(this.pluginInputStreamProvider, flags, this.classLoader);
        this.classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
