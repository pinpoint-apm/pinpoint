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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.instrument.scanner.ClassScannerFactory;
import com.navercorp.pinpoint.profiler.instrument.scanner.Scanner;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMClassNodeAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ASMClassNodeAdapter.class);

    public static ASMClassNodeAdapter get(final ClassInputStreamProvider pluginClassInputStreamProvider, final ClassLoader classLoader, ProtectionDomain protectionDomain, final String classInternalName) {
        return get(pluginClassInputStreamProvider, classLoader, protectionDomain, classInternalName, false);
    }

    public static ASMClassNodeAdapter get(final ClassInputStreamProvider pluginClassInputStreamProvider, final ClassLoader classLoader, ProtectionDomain protectionDomain, final String classInternalName, final boolean skipCode) {
        Assert.requireNonNull(pluginClassInputStreamProvider, "pluginInputStreamProvider");
        Assert.requireNonNull(classInternalName, "classInternalName");

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
            return new String[0];
        }

        final List<String> list = new ArrayList<String>(interfaces.size());
        for (String name : interfaces) {
            if (name != null) {
                list.add(JavaAssistUtils.jvmNameToJavaName(name));
            }
        }

        return list.toArray(new String[0]);
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
        if (methodName == null) {
            return null;
        }

        final List<MethodNode> declaredMethods = classNode.methods;
        if (declaredMethods == null) {
            return null;
        }

        for (MethodNode methodNode : declaredMethods) {
            if (!strEquals(methodNode.name, methodName)) {
                continue;
            }

            if (desc == null || startWith(methodNode.desc, desc)) {
                return new ASMMethodNodeAdapter(getInternalName(), methodNode);
            }
        }

        return null;
    }

    private List<ASMMethodNodeAdapter> findDeclaredMethod(final String methodName) {
        if (methodName == null) {
            return null;
        }

        final List<MethodNode> declaredMethods = classNode.methods;
        if (declaredMethods == null) {
            return null;
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<ASMMethodNodeAdapter>();
        for (MethodNode methodNode : declaredMethods) {
            if (!strEquals(methodNode.name, methodName)) {
                continue;
            }

            methodNodes.add(new ASMMethodNodeAdapter(getInternalName(), methodNode));
        }
        return methodNodes;
    }

    private static boolean startWith(String str1, String str2) {
        if (str1 == null) {
            return false;
        }
        return str1.startsWith(str2);
    }

    private static boolean strEquals(String str1, String str2) {
        if (str1 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    public List<ASMMethodNodeAdapter> getDeclaredMethods() {
        if (this.skipCode) {
            throw new IllegalStateException("not supported operation, skipCode option is true.");
        }

        final List<MethodNode> methods = this.classNode.methods;
        if (methods == null) {
            return Collections.emptyList();
        }

        final List<ASMMethodNodeAdapter> methodNodes = new ArrayList<ASMMethodNodeAdapter>(methods.size());
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
        if (startWith(this.classNode.outerMethodDesc, desc)) {
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
            final ASMClassNodeAdapter classNode = ASMClassNodeAdapter.get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, this.classNode.superName, true);
            if (classNode != null) {
                return classNode.hasMethod(methodName, desc);
            }
        }

        return false;
    }

    public ASMFieldNodeAdapter getField(final String fieldName, final String fieldDesc) {
        if (fieldName == null) {
            return null;
        }
        if (this.classNode.fields == null) {
            return null;
        }

        final List<FieldNode> fields = this.classNode.fields;
        for (FieldNode fieldNode : fields) {
            if (strEquals(fieldNode.name, fieldName) && (fieldDesc == null || (strEquals(fieldNode.desc, fieldDesc)))) {
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
        if (this.classNode.superName != null) {
            final ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter.get(this.pluginInputStreamProvider, this.classLoader, this.protectionDomain, this.classNode.superName, true);
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
        Assert.requireNonNull(fieldName, "fieldName");
        Assert.requireNonNull(fieldDesc, "fieldDesc");
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
            this.classNode.fields = new ArrayList<FieldNode>();
        }
        this.classNode.fields.add(fieldNode);
    }

    public ASMMethodNodeAdapter addDelegatorMethod(final ASMMethodNodeAdapter superMethodNode) {
        Assert.requireNonNull(superMethodNode, "superMethodNode");

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
        return superMethodNodeExceptions.toArray(new String[0]);
    }

    public void addGetterMethod(final String methodName, final ASMFieldNodeAdapter fieldNode) {
        Assert.requireNonNull(methodName, "methodName");
        Assert.requireNonNull(fieldNode, "fieldNode");


        // no argument is ().
        final String desc = "()" + fieldNode.getDesc();
        final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        final InsnList instructions = getInsnList(methodNode);
        // load this.
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        // get fieldNode.
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldNode.getName(), fieldNode.getDesc()));
        // return of type.
        final Type type = Type.getType(fieldNode.getDesc());
        instructions.add(new InsnNode(type.getOpcode(Opcodes.IRETURN)));

        addMethodNode0(methodNode);
    }

    private void addMethodNode0(MethodNode methodNode) {
        if (this.classNode.methods == null) {
            this.classNode.methods = new ArrayList<MethodNode>();
        }
        this.classNode.methods.add(methodNode);
    }

    public void addSetterMethod(final String methodName, final ASMFieldNodeAdapter fieldNode) {
        Assert.requireNonNull(methodName, "methodName");
        Assert.requireNonNull(fieldNode, "fieldNode");


        // void is V.
        final String desc = "(" + fieldNode.getDesc() + ")V";
        final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        final InsnList instructions = getInsnList(methodNode);
        // load this.
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        final Type type = Type.getType(fieldNode.getDesc());
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
        Assert.requireNonNull(interfaceName, "interfaceName");

        if (this.classNode.interfaces == null) {
            this.classNode.interfaces = new ArrayList<String>();
        }
        this.classNode.interfaces.add(JavaAssistUtils.javaNameToJvmName(interfaceName));
    }

    public void copyMethod(final ASMMethodNodeAdapter methodNode) {
        Assert.requireNonNull(methodNode, "methodNode");

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
            if (strEquals(annotation.desc, annotationClassDesc)) {
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

        final List<ASMClassNodeAdapter> innerClasses = new ArrayList<ASMClassNodeAdapter>();
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
