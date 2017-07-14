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

import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class ASMFieldNodeAdapterTest {
    @Test
    public void getName() throws Exception {
        ClassNode classNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        List<FieldNode> fieldNodes = classNode.fields;
        for (FieldNode fieldNode : fieldNodes) {
            ASMFieldNodeAdapter adapter = new ASMFieldNodeAdapter(fieldNode);
            assertNotNull(adapter.getName());
            assertNotNull(adapter.getClassName());
            assertNotNull(adapter.getDesc());
        }
    }
}
