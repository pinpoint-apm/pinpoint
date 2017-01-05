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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMAspectWeaver {

    private static final MethodNameReplacer DEFAULT_METHOD_NAME_REPLACER = new DefaultMethodNameReplacer();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MethodNameReplacer methodNameReplacer;

    public ASMAspectWeaver() {
        this(DEFAULT_METHOD_NAME_REPLACER);
    }

    public ASMAspectWeaver(final MethodNameReplacer methodNameReplacer) {
        this.methodNameReplacer = methodNameReplacer;
    }

    public void weaving(final ASMClassNodeAdapter sourceClassNode, final ASMClassNodeAdapter adviceClassNode) throws InstrumentException {
        if (sourceClassNode == null || adviceClassNode == null) {
            throw new InstrumentException("source and advice class node must not be null.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("weaving sourceClass={} adviceClass={}", sourceClassNode.getInternalName(), adviceClassNode.getInternalName());
        }

        if (!adviceClassNode.hasAnnotation(Aspect.class)) {
            throw new InstrumentException("@Aspect not found. adviceClass=" + adviceClassNode.getInternalName());
        }

        // advice class hierarchy check.
        final boolean isSubclass = adviceClassNode.subclassOf(sourceClassNode.getInternalName());
        if (!isSubclass) {
            final String superClassInternalName = adviceClassNode.getSuperClassInternalName();
            if (superClassInternalName == null || !superClassInternalName.equals("java/lang/Object")) {
                throw new InstrumentException("invalid class hierarchy. source class=" + sourceClassNode.getInternalName() + ", advice class=" + adviceClassNode.getInternalName() + ", super class=" + superClassInternalName);
            }
        }

        // find annotation methods and copy utility methods.
        final MethodNodes methodNodes = findMethodNodes(adviceClassNode);

        copyUtilMethods(methodNodes, sourceClassNode);
        copyPointCutMethods(methodNodes, sourceClassNode);
    }

    private MethodNodes findMethodNodes(final ASMClassNodeAdapter adviceClassNode) {
        final MethodNodes methodNodes = new MethodNodes();
        for (ASMMethodNodeAdapter methodNode : adviceClassNode.getDeclaredMethods()) {
            if (methodNode.hasAnnotation(PointCut.class)) {
                methodNodes.pointCuts.add(methodNode);
                continue;
            }
            if (methodNode.hasAnnotation(JointPoint.class)) {
                methodNodes.jointPoints.add(methodNode);
                continue;
            }

            methodNodes.utils.add(methodNode);
        }

        return methodNodes;
    }

    private void copyUtilMethods(final MethodNodes methodNodes, final ASMClassNodeAdapter classNode) throws InstrumentException {
        for (ASMMethodNodeAdapter methodNode : methodNodes.utils) {
            if (!methodNode.isPrivate()) {
                throw new InstrumentException("non private UtilMethod unsupported. method=" + methodNode.getLongName());
            }

            classNode.copyMethod(methodNode);
        }
    }

    private void copyPointCutMethods(final MethodNodes methodNodes, final ASMClassNodeAdapter classNode) throws InstrumentException {
        final ASMMethodInsnNodeRemapper remapper = new ASMMethodInsnNodeRemapper();
        for (ASMMethodNodeAdapter joinPointMethodNode : methodNodes.jointPoints) {
            remapper.addFilter(null, joinPointMethodNode.getName(), joinPointMethodNode.getDesc());
        }

        for (ASMMethodNodeAdapter pointCutMethodNode : methodNodes.pointCuts) {
            final ASMMethodNodeAdapter sourceMethodNode = classNode.getDeclaredMethod(pointCutMethodNode.getName(), pointCutMethodNode.getDesc());
            if (sourceMethodNode == null) {
                throw new InstrumentException("not found method. " + classNode.getInternalName() + "." + pointCutMethodNode.getName());
            }

            if (!sourceMethodNode.getDesc().equals(pointCutMethodNode.getDesc())) {
                throw new InstrumentException("Signature miss match. method=" + pointCutMethodNode.getName() + ", source=" + sourceMethodNode.getDesc() + ", advice=" + pointCutMethodNode.getDesc());
            }

            if (logger.isInfoEnabled()) {
                logger.info("weaving method={}{}", sourceMethodNode.getName(), sourceMethodNode.getDesc());
            }

            // rename.
            final String methodName = this.methodNameReplacer.replaceMethodName(sourceMethodNode.getName());
            sourceMethodNode.rename(methodName);
            // set private.
            sourceMethodNode.setAccess(sourceMethodNode.getAccess() & -6 | Opcodes.ACC_PRIVATE);
            // copy.
            classNode.copyMethod(pointCutMethodNode);

            // remap
            final ASMMethodNodeAdapter newMethodNode = classNode.getDeclaredMethod(pointCutMethodNode.getName(), pointCutMethodNode.getDesc());
            if (newMethodNode == null) {
                throw new InstrumentException("not found new method. " + classNode.getInternalName() + "." + pointCutMethodNode.getName());
            }
            remapper.setName(methodName);
            newMethodNode.remapMethodInsnNode(remapper);
        }
    }

    private static class MethodNodes {
        public final List<ASMMethodNodeAdapter> pointCuts = new ArrayList<ASMMethodNodeAdapter>();
        public final List<ASMMethodNodeAdapter> jointPoints = new ArrayList<ASMMethodNodeAdapter>();
        public final List<ASMMethodNodeAdapter> utils = new ArrayList<ASMMethodNodeAdapter>();
    }
}