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

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;

import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class ASMBytecodeDisassemblerTest {
    @Test
    public void dumpBytecode() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, "java/lang/String");

        ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();
        String result = bytecodeDisassembler.dumpBytecode(classFile);

        bytecodeDisassembler = new ASMBytecodeDisassembler(0, 0);
        String result2 = bytecodeDisassembler.dumpBytecode(classFile);
        assertEquals(result, result2);
    }

    @Test
    public void dumpASM() throws Exception {
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, "java/lang/String");

        ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();
        String result = bytecodeDisassembler.dumpASM(classFile);
        assertNotNull(result);
    }

    @Test
    public void dumpVerify() throws Exception {
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, "java/lang/String");

        ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();
        String result = bytecodeDisassembler.dumpVerify(classFile, classLoader);
        assertNotNull(result);
    }
}