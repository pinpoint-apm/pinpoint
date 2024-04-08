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

import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolder;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

public class ASMInterceptorHolderTest {

    @Test
    public void mainClass() throws Exception {
        ClassLoader classLoader = InterceptorHolder.class.getClassLoader();
        final String classFileName = JavaAssistUtils.javaNameToJvmName(InterceptorHolder.class.getName()) + ".class";

        final ClassReader classReader = new ClassReader(classLoader.getResourceAsStream(classFileName));
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        RenameClassAdapter renameClassAdapter = new RenameClassAdapter(ASMVersion.VERSION, classWriter, "foo.bar.Interceptor111");
        classNode.accept(renameClassAdapter);
        byte[] bytes = classWriter.toByteArray();

        ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();
        String result = bytecodeDisassembler.dumpBytecode(bytes);
        System.out.println("## Main Class");
        System.out.println(result);
    }

    @Test
    public void innerClass() throws Exception {
        ClassLoader classLoader = InterceptorHolder.class.getClassLoader();
        final String classFileName = JavaAssistUtils.javaNameToJvmName(InterceptorHolder.class.getName()) + "$LazyLoading.class";

        final ClassReader classReader = new ClassReader(classLoader.getResourceAsStream(classFileName));
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        RenameInnerClassAdapter renameClassAdapter = new RenameInnerClassAdapter(ASMVersion.VERSION, classWriter, "foo.bar.Interceptor111");
        classNode.accept(renameClassAdapter);
        byte[] bytes = classWriter.toByteArray();

        ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();
        String result = bytecodeDisassembler.dumpBytecode(bytes);
        System.out.println("## Inner Class");
        System.out.println(result);
    }


    class RenameClassAdapter extends ClassVisitor {
        private final String newInternalName;

        public RenameClassAdapter(int api, ClassVisitor classVisitor, String name) {
            super(api, classVisitor);
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

    class RenameInnerClassAdapter extends ClassVisitor {
        private final String newInternalName;

        public RenameInnerClassAdapter(int api, ClassVisitor classVisitor, String name) {
            super(api, classVisitor);
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
