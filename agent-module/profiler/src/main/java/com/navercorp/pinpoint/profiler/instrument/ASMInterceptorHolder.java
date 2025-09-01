/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.instrument.classloading.InterceptorDefineClassHelper;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolderIdGenerator;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorLazyLoadingSupplier;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorSupplier;
import com.navercorp.pinpoint.profiler.interceptor.factory.InterceptorFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

public class ASMInterceptorHolder {
    private static final String INTERCEPTOR_HOLDER_CLASS = "com/navercorp/pinpoint/profiler/instrument/interceptor/InterceptorHolder.class";
    private static final String INTERCEPTOR_HOLDER_INNER_CLASS = "com/navercorp/pinpoint/profiler/instrument/interceptor/InterceptorHolder$LazyLoading.class";
    private static final String CLASS_NAME = "com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolder$$";
    private static final String BOOTSTRAP_CLASS_NAME = "com.navercorp.pinpoint.bootstrap.interceptor.holder.InterceptorHolder$$";
    private static final String REFLECTION_CLASS_LOADER_CLASS_NAME = "jdk.internal.reflect.DelegatingClassLoader";
    private static final int EMPTY_ID = -1;

    public static ASMInterceptorHolder create(InterceptorHolderIdGenerator interceptorHolderIdGenerator, ClassLoader classLoader, InterceptorFactory interceptorFactory, Class<? extends Interceptor> interceptorClass, Object[] providedArguments, ScopeInfo scopeInfo, MethodDescriptor methodDescriptor) throws InstrumentException {
        Builder builder = new Builder(interceptorHolderIdGenerator);
        builder.interceptorFactory(classLoader, interceptorFactory, interceptorClass, providedArguments, scopeInfo, methodDescriptor);
        return builder.build();
    }

    // for test
    public static ASMInterceptorHolder create(InterceptorHolderIdGenerator interceptorHolderIdGenerator, ClassLoader classLoader, Interceptor interceptor) throws InstrumentException {
        Builder builder = new Builder(interceptorHolderIdGenerator);
        builder.interceptor(classLoader, interceptor);
        return builder.build();
    }

    private final int interceptorId;
    private final String className;
    private final String innerClassName;

    public ASMInterceptorHolder(int interceptorId, boolean bootstrap) {
        this.interceptorId = interceptorId;
        if (bootstrap) {
            this.className = BOOTSTRAP_CLASS_NAME + interceptorId;
        } else {
            this.className = CLASS_NAME + interceptorId;
        }
        this.innerClassName = className + "$LazyLoading";
    }

    public int getInterceptorId() {
        return interceptorId;
    }

    public String getClassName() {
        return className;
    }

    public boolean isEmpty() {
        return interceptorId == EMPTY_ID;
    }

    public Class<? extends Interceptor> loadInterceptorClass(ClassLoader classLoader) throws InstrumentException {
        try {
            final Class<?> clazz = loadClass(Boolean.FALSE, classLoader);
            if (clazz == null) {
                // defense code
                throw new InstrumentException("not found interceptorHolderClass, className=" + className);
            }

            final Method method = clazz.getDeclaredMethod("get");
            final Object o = method.invoke(null);
            if (o instanceof Interceptor) {
                return (Class<? extends Interceptor>) o.getClass();
            } else {
                throw new InstrumentException("not found interceptor, className=" + className);
            }
        } catch (InvocationTargetException e) {
            throw new InstrumentException("invocation fail, className=" + className, e);
        } catch (NoSuchMethodException e) {
            throw new InstrumentException("not found 'get' method, className=" + className, e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException("access fail, className=" + className, e);
        }
    }

    public void init(Class<?> interceptorHolderClass, InterceptorFactory factory, Class<? extends Interceptor> interceptorClass, Object[] providedArguments, ScopeInfo scopeInfo, MethodDescriptor methodDescriptor) throws InstrumentException {
        init(interceptorHolderClass, new InterceptorLazyLoadingSupplier(factory, interceptorClass, providedArguments, scopeInfo, methodDescriptor));
    }

    public void init(Class<?> interceptorHolderClass, Interceptor interceptor) throws InstrumentException {
        init(interceptorHolderClass, new InterceptorSupplier(interceptor));
    }

    private void init(Class<?> interceptorHolderClass, Supplier<Interceptor> supplier) throws InstrumentException {
        try {
            final Method method = interceptorHolderClass.getDeclaredMethod("set", Supplier.class);
            method.invoke(null, supplier);
        } catch (NoSuchMethodException e) {
            throw new InstrumentException("not found 'set' method, className=" + interceptorHolderClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException("access fail, className=" + interceptorHolderClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new InstrumentException("invocation fail, className=" + interceptorHolderClass.getName(), e);
        }
    }

    public Class<?> loadClass(boolean initialize, ClassLoader classLoader) throws InstrumentException {
        try {
            return Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException e) {
            throw new InstrumentException("not found InterceptorHolderClass, className=" + className);
        }
    }

    public Class<?> defineClass(ClassLoader classLoader) throws InstrumentException {
        try {
            final byte[] mainClassBytes = toMainClassByteArray();
            final Class<?> mainClass = InterceptorDefineClassHelper.defineClass(classLoader, className, mainClassBytes);
            final byte[] innerClassByte = toInnerClassByteArray();
            InterceptorDefineClassHelper.defineClass(classLoader, innerClassName, innerClassByte);
            return mainClass;
        } catch (Exception e) {
            throw new InstrumentException("defineClass fail", e);
        }
    }

    byte[] toMainClassByteArray() throws InstrumentException {
        try {
            ClassNode classNode = readClass(INTERCEPTOR_HOLDER_CLASS);
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            final ClassVisitor renameClassAdapter = new RenameClassAdapter(classWriter, className);
            classNode.accept(renameClassAdapter);
            return classWriter.toByteArray();
        } catch (IOException e) {
            // read fail
            throw new InstrumentException("ClassReader fail, classFile=" + INTERCEPTOR_HOLDER_CLASS, e);
        }
    }

    byte[] toInnerClassByteArray() throws InstrumentException {
        try {
            ClassNode classNode = readClass(INTERCEPTOR_HOLDER_INNER_CLASS);
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            final ClassVisitor renameClassAdapter = new RenameInnerClassAdapter(classWriter, className);
            classNode.accept(renameClassAdapter);
            return classWriter.toByteArray();
        } catch (IOException e) {
            // read fail
            throw new InstrumentException("ClassReader fail, classFile=" + INTERCEPTOR_HOLDER_INNER_CLASS, e);
        }
    }

    ClassNode readClass(String classFileName) throws IOException {
        final ClassLoader classLoader = ASMInterceptorHolder.class.getClassLoader();
        final InputStream inputStram = classLoader.getResourceAsStream(classFileName);
        if (inputStram == null) {
            // defense code
            throw new IOException("not found class " + classFileName);
        }

        try {
            final ClassReader classReader = new ClassReader(inputStram);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        } finally {
            inputStram.close();
        }
    }

    static class Builder {
        private final Logger logger = LogManager.getLogger(this.getClass());
        private final InterceptorHolderIdGenerator interceptorHolderIdGenerator;
        private int interceptorId;
        private ClassLoader classLoader;
        private InterceptorFactory interceptorFactory;
        private Class<? extends Interceptor> interceptorClass;
        private Object[] providedArguments;
        private ScopeInfo scopeInfo;
        private MethodDescriptor methodDescriptor;
        private Interceptor interceptor;

        public Builder(InterceptorHolderIdGenerator interceptorHolderIdGenerator) {
            this.interceptorHolderIdGenerator = Objects.requireNonNull(interceptorHolderIdGenerator, "interceptorHolderIdGenerator");
        }

        public Builder interceptorFactory(ClassLoader classLoader, InterceptorFactory interceptorFactory, Class<? extends Interceptor> interceptorClass, Object[] providedArguments, ScopeInfo scopeInfo, MethodDescriptor methodDescriptor) {
            this.classLoader = classLoader;
            this.interceptorFactory = Objects.requireNonNull(interceptorFactory, "interceptorFactory");
            this.interceptorClass = Objects.requireNonNull(interceptorClass, "interceptorClass");
            this.providedArguments = providedArguments;
            this.scopeInfo = scopeInfo;
            this.methodDescriptor = methodDescriptor;
            return this;
        }

        public Builder interceptor(ClassLoader classLoader, Interceptor interceptor) {
            this.classLoader = classLoader;
            this.interceptor = Objects.requireNonNull(interceptor, "interceptor");
            return this;
        }

        public ASMInterceptorHolder build() throws InstrumentException {
            ASMInterceptorHolder holder;
            Class<?> clazz;
            if (classLoader == null || REFLECTION_CLASS_LOADER_CLASS_NAME.equals(classLoader.getClass().getName())) {
                try {
                    interceptorId = interceptorHolderIdGenerator.getBootstrapId();
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    logger.warn("Failed to generate bootstrap interceptorHolder id, cause={}", indexOutOfBoundsException.getMessage());
                    return new ASMInterceptorHolder(EMPTY_ID, Boolean.TRUE);
                }
                holder = new ASMInterceptorHolder(interceptorId, Boolean.TRUE);
                // load and initialize
                clazz = holder.loadClass(Boolean.TRUE, classLoader);
                interceptor = interceptorFactory.newInterceptor(interceptorClass, providedArguments, scopeInfo, methodDescriptor);
            } else {
                try {
                    interceptorId = interceptorHolderIdGenerator.getId();
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    logger.warn("Failed to generate interceptorHolder id, cause={}", indexOutOfBoundsException.getMessage());
                    return new ASMInterceptorHolder(EMPTY_ID, Boolean.FALSE);
                }
                holder = new ASMInterceptorHolder(interceptorId, Boolean.FALSE);
                // define class
                clazz = holder.defineClass(classLoader);
            }
            // exception handling.
            if (interceptor != null) {
                holder.init(clazz, interceptor);
            } else if (interceptorFactory != null) {
                holder.init(clazz, interceptorFactory, interceptorClass, providedArguments, scopeInfo, methodDescriptor);
            } else {
                throw new InstrumentException("either interceptor or interceptorFactory must be present.");
            }

            return holder;
        }
    }

    static class RenameClassAdapter extends ClassVisitor {
        private final String newInternalName;

        public RenameClassAdapter(ClassVisitor classVisitor, String name) {
            super(ASMVersion.VERSION, classVisitor);
            this.newInternalName = JavaAssistUtils.javaNameToJvmName(name);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, this.newInternalName, signature, superName, interfaces);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(newInternalName + "$" + innerName, newInternalName, innerName, access);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals("<init>")) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        // LOCALVARIABLE this Lcom/navercorp/pinpoint/profiler/instrument/mock/InterceptorHolder; L0 L1 0
                        if (name.equals("this")) {
                            final String newDescriptor = "L" + newInternalName + ";";
                            super.visitLocalVariable(name, newDescriptor, signature, start, end, index);
                        } else {
                            super.visitLocalVariable(name, descriptor, signature, start, end, index);
                        }
                    }
                };
            } else if (name.equals("get")) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        // GETSTATIC com/navercorp/pinpoint/profiler/instrument/mock/InterceptorHolder.holder : Lcom/navercorp/pinpoint/bootstrap/interceptor/Interceptor;
                        final int innerNameStartPosition = owner.indexOf('$');
                        if (innerNameStartPosition != -1) {
                            final String innerName = owner.substring(innerNameStartPosition);
                            super.visitFieldInsn(opcode, newInternalName + innerName, name, descriptor);
                        } else {
                            super.visitFieldInsn(opcode, newInternalName, name, descriptor);
                        }
                    }
                };
            } else if (name.equals("set")) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        // PUTSTATIC com/navercorp/pinpoint/profiler/instrument/mock/InterceptorBinder.binder : Lcom/navercorp/pinpoint/bootstrap/interceptor/Interceptor;
                        super.visitFieldInsn(opcode, newInternalName, name, descriptor);
                    }
                };
            } else if (name.equals("access$000")) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        // GETSTATIC com/navercorp/pinpoint/profiler/instrument/mock/InterceptorBinder.factory : Lcom/navercorp/pinpoint/profiler/instrument/mock/InterceptorLazyFactory;
                        super.visitFieldInsn(opcode, newInternalName, name, descriptor);
                    }
                };
            }

            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }

    static class RenameInnerClassAdapter extends ClassVisitor {
        private final String newInternalName;

        public RenameInnerClassAdapter(ClassVisitor classVisitor, String name) {
            super(ASMVersion.VERSION, classVisitor);
            this.newInternalName = JavaAssistUtils.javaNameToJvmName(name);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            final int innerNameStartPosition = name.indexOf('$');
            if (innerNameStartPosition != -1) {
                final String innerName = name.substring(innerNameStartPosition);
                super.visit(version, access, this.newInternalName + innerName, signature, superName, interfaces);
            } else {
                super.visit(version, access, this.newInternalName, signature, superName, interfaces);
            }
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(newInternalName + "$" + innerName, newInternalName, innerName, access);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals("<init>")) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        // LOCALVARIABLE this Lcom/navercorp/pinpoint/profiler/instrument/mock/InterceptorHolder; L0 L1 0
                        if (name.equals("this")) {
                            final int innerNameStartPosition = descriptor.indexOf('$');
                            if (innerNameStartPosition != -1) {
                                final String innerName = descriptor.substring(innerNameStartPosition);
                                final String newDescriptor = "L" + newInternalName + innerName;
                                super.visitLocalVariable(name, newDescriptor, signature, start, end, index);
                            } else {
                                super.visitLocalVariable(name, descriptor, signature, start, end, index);
                            }
                        } else {
                            super.visitLocalVariable(name, descriptor, signature, start, end, index);
                        }
                    }
                };
            } else if (name.equals("<clinit>")) {
                return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (name.equals("access$000")) {
                            super.visitMethodInsn(opcode, newInternalName, name, descriptor, isInterface);
                        } else {
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        // GETSTATIC com/navercorp/pinpoint/profiler/instrument/mock/InterceptorHolder.holder : Lcom/navercorp/pinpoint/bootstrap/interceptor/Interceptor;
                        final int innerNameStartPosition = owner.indexOf('$');
                        if (innerNameStartPosition != -1) {
                            final String innerName = owner.substring(innerNameStartPosition);
                            super.visitFieldInsn(opcode, newInternalName + innerName, name, descriptor);
                        } else {
                            super.visitFieldInsn(opcode, newInternalName, name, descriptor);
                        }
                    }
                };
            }

            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }
}
