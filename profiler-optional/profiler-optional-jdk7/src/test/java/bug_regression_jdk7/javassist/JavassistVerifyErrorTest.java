/*
 * Copyright 2016 Naver Corp.
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

package bug_regression_jdk7.javassist;

import bug_regression_jdk7.javassist.asm.BytecodeVerifyTestClassVisitor;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDisassembler;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JavassistVerifyErrorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INVALID_STACK_MAP_FRAME = "bug_regression_jdk7.javassist.InvalidStackMapFrame";

    /**
     * bug id
     * https://github.com/naver/pinpoint/issues/1807
     * @throws Exception
     */
    @Ignore("fixed Javassist 3.21.0-GA")
    @Test
    public void bug_regression_BytecodeVerifyError_Invalid_StackMapFrame() throws Exception {

        CustomURLClassLoader classLoader = new CustomURLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());

        ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(classLoader));

        final CtClass ctClass = classPool.get(INVALID_STACK_MAP_FRAME);
        final CtMethod method = ctClass.getDeclaredMethod("bytecodeVerifyError");
        method.addLocalVariable("test_localVariable", CtClass.intType);
        method.insertBefore("{ test_localVariable = 1; }");

        final byte[] bytecode = ctClass.toBytecode();
        classLoader.defineClass0(INVALID_STACK_MAP_FRAME, bytecode);
        try {
            Class.forName(INVALID_STACK_MAP_FRAME, true, classLoader);
            Assert.fail("VerifyError");
        } catch (VerifyError e) {
            logger.debug("verifyError:{}", e.getMessage(), e);
        }


        final ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();

        final String dumpBytecode = bytecodeDisassembler.dumpBytecode(bytecode);
        logger.debug("dumpBytecode:{}", dumpBytecode);

//        javassist bug : invalid stack map frame
//        00013 InvalidStackMapFrame ArrayList String Iterator I  :  :    FRAME FULL [bug_regression_jdk7/javassist/InvalidStackMapFrame java/util/ArrayList [[[java/lang/Object->[Ljava/lang/String;]]] java/util/Iterator T T T I] []
        final String verify = bytecodeDisassembler.dumpVerify(bytecode, classLoader);
        logger.debug("dumpVerify:{}", verify);

        final String dumpAsm = bytecodeDisassembler.dumpASM(bytecode);
        logger.debug("dumpAsm :{}", dumpAsm);

    }


    @Test
    public void asm_stackmapframe_check() throws Exception {

        CustomURLClassLoader classLoader = new CustomURLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());
        final InputStream stream = classLoader.getResourceAsStream(JavaAssistUtils.javaNameToJvmName(INVALID_STACK_MAP_FRAME) + ".class");

        ClassReader cr = new ClassReader(stream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new BytecodeVerifyTestClassVisitor(cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);

        byte[] bytecode = cw.toByteArray();
        classLoader.defineClass0(INVALID_STACK_MAP_FRAME, bytecode);

        final Class<?> aClass = Class.forName(INVALID_STACK_MAP_FRAME, true, classLoader);
        Assert.assertSame(aClass.getClassLoader(), classLoader);


        final ASMBytecodeDisassembler bytecodeDisassembler = new ASMBytecodeDisassembler();

        final String dumpBytecode = bytecodeDisassembler.dumpBytecode(bytecode);
        logger.debug("dumpBytecode:{}", dumpBytecode);

        final String verify = bytecodeDisassembler.dumpVerify(bytecode, classLoader);
        logger.debug("dumpVerify:{}", verify);

//        final String dumpAsm = bytecodeDisassembler.dumpASM(bytecode);
//        logger.debug("dumpAsm :{}", dumpAsm);

    }
}
