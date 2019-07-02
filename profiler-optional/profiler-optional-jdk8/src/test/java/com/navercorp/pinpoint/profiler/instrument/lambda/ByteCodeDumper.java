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

import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDisassembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ByteCodeDumper {
    private static final Logger logger = LoggerFactory.getLogger(ByteCodeDumper.class);

    private static final ASMBytecodeDisassembler disassembler = new ASMBytecodeDisassembler();

    public static void dumpByteCode(byte[] bytes) {
        String asmCode = disassembler.dumpASM(bytes);
        logger.trace("asm----- \n{}", asmCode);


        String byteCode = disassembler.dumpBytecode(bytes);
        logger.trace("bytecode---- \n{}", byteCode);

    }

    public static void verify(byte[] bytes, ClassLoader classLoader) {
        String verify = disassembler.dumpVerify(bytes, classLoader);
        logger.trace("verify---- \n{}", verify);
    }
}
