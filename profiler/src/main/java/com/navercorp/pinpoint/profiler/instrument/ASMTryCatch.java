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

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author jaehong.kim
 */
public class ASMTryCatch {
    private final MethodNode methodNode;
    private final LabelNode startLabelNode = new LabelNode();
    private final LabelNode endLabelNode = new LabelNode();

    public ASMTryCatch(final MethodNode methodNode) {
        this.methodNode = methodNode;

        final TryCatchBlockNode tryCatchBlockNode = new TryCatchBlockNode(this.startLabelNode, this.endLabelNode, this.endLabelNode, "java/lang/Throwable");
        if (this.methodNode.tryCatchBlocks == null) {
            this.methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
        }
        this.methodNode.tryCatchBlocks.add(tryCatchBlockNode);
    }

    public LabelNode getStartLabelNode() {
        return this.startLabelNode;
    }

    public LabelNode getEndLabelNode() {
        return this.endLabelNode;
    }

    public void sort() {
        if (this.methodNode.tryCatchBlocks == null) {
            return;
        }

        Collections.sort(this.methodNode.tryCatchBlocks, new Comparator<TryCatchBlockNode>() {
            @Override
            public int compare(TryCatchBlockNode o1, TryCatchBlockNode o2) {
                return blockLength(o1) - blockLength(o2);
            }

            private int blockLength(TryCatchBlockNode block) {
                final int startidx = methodNode.instructions.indexOf(block.start);
                final int endidx = methodNode.instructions.indexOf(block.end);
                return endidx - startidx;
            }
        });

        // Updates the 'target' of each try catch block annotation.
        for (int i = 0; i < this.methodNode.tryCatchBlocks.size(); ++i) {
            this.methodNode.tryCatchBlocks.get(i).updateIndex(i);
        }
    }
}