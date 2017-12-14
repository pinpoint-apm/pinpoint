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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

/**
 * @author jaehong.kim
 */
public class ASMFieldNodeAdapter {
    private FieldNode fieldNode;

    public ASMFieldNodeAdapter(final FieldNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    public String getName() {
        return this.fieldNode.name;
    }

    public String getClassName() {
        Type type = Type.getType(this.fieldNode.desc);
        return type.getClassName();
    }

    public String getDesc() {
        return this.fieldNode.desc;
    }

    public boolean isStatic() {
        return (this.fieldNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isFinal() {
        return (this.fieldNode.access & Opcodes.ACC_FINAL) != 0;
    }

    public void setAccess(final int access) {
        this.fieldNode.access = access;
    }

    public int getAccess() {
        return this.fieldNode.access;
    }
}
