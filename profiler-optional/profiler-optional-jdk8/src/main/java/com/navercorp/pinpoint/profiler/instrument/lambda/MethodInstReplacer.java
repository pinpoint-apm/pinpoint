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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.ASMVersion;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MethodInstReplacer extends ClassVisitor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String className;
    private final String methodName;
    private final String targetClassName;
    private final String targetMethodName;
    private final String delegateClassName;
    private final String delegateMethodName;
    private int transformCount = 0;

    public MethodInstReplacer(ClassVisitor classVisitor, String methodName,
                              String targetClassName, String targetMethodName,
                              String delegateClassName, String delegateMethodName) {
        super(ASMVersion.VERSION, classVisitor);
        this.methodName = Assert.requireNonNull(methodName, "methodName");

        this.targetClassName = Assert.requireNonNull(targetClassName, "targetClassName");
        this.targetMethodName = Assert.requireNonNull(targetMethodName, "targetMethodName");

        this.delegateClassName = Assert.requireNonNull(delegateClassName, "delegateClassName");
        this.delegateMethodName = Assert.requireNonNull(delegateMethodName, "delegateMethodName");
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (methodName.equals(name)) {
            logger.info("visitMethod {} desc:{} {}", name, descriptor);

            final MethodVisitor superMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MethodVisitor(ASMVersion.VERSION, superMethodVisitor) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (targetClassName.equals(owner) && targetMethodName.equals(name)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("replace MethodInsn {}.{}() -> {}.{}()", owner, name, delegateClassName, delegateMethodName);
                        }
                        transformCount++;
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, delegateClassName, delegateMethodName, descriptor, isInterface);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                }
            };
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
