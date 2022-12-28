/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.lambda;

import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.profiler.instrument.ASMVersion;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MethodInstReplacer extends ClassVisitor {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private String className;
    private int transformCount = 0;
    private final List<MethodInsn> methodInsnList;

    public MethodInstReplacer(ClassVisitor classVisitor, List<MethodInsn> methodInsnList) {
        super(ASMVersion.VERSION, classVisitor);
        this.methodInsnList = Objects.requireNonNull(methodInsnList, "methodInsnList");
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        for (MethodInsn methodInsn : methodInsnList) {
            if (methodInsn.getMethodName().equals(name)) {
                logger.info("visitMethod {} desc:{} {}", name, descriptor);

                final MethodVisitor superMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(ASMVersion.VERSION, superMethodVisitor) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (methodInsn.getTargetClassName().equals(owner) && methodInsn.getTargetMethodName().equals(name)) {
                            if (logger.isInfoEnabled()) {
                                logger.info("replace MethodInsn {}.{}() -> {}.{}()", owner, name, methodInsn.getDelegateClassName(), methodInsn.getDelegateMethodName());
                            }
                            transformCount++;
                            if (methodInsn.getDelegateDescriptor() != null) {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, methodInsn.getDelegateClassName(), methodInsn.getDelegateMethodName(), methodInsn.getDelegateDescriptor(), isInterface);
                            } else {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, methodInsn.getDelegateClassName(), methodInsn.getDelegateMethodName(), descriptor, isInterface);
                            }
                        } else {
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    }
                };
            }
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    public String getClassName() {
        return className;
    }

    public int getTransformCount() {
        return transformCount;
    }
}
