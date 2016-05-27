package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.test.util.BytecodeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.times;

/**
 * @author Woonduk Kang(emeroad)
 */
@RunWith(MockitoJUnitRunner.class)
public class BytecodeDumpServiceTest {

    private final String jvmClassName = JavaAssistUtils.javaNameToJvmName("java.lang.String");;

    @Mock
    private ASMBytecodeDisassembler disassembler;

    @InjectMocks
    private BytecodeDumpService bytecodeDumpService = new ASMBytecodeDumpService(true, true, true, Collections.singletonList(jvmClassName));

    @InjectMocks
    private BytecodeDumpService disableBytecodeDumpService = new ASMBytecodeDumpService(false, false, false, Collections.singletonList(jvmClassName));

    @Test
    public void dumpBytecode() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, jvmClassName);


        bytecodeDumpService.dumpBytecode("testDump", jvmClassName, classFile, classLoader);

        Mockito.verify(this.disassembler, times(1)).dumpBytecode(classFile);
        Mockito.verify(this.disassembler, times(1)).dumpVerify(classFile, classLoader);
        Mockito.verify(this.disassembler, times(1)).dumpASM(classFile);
    }


    @Test
    public void dumpBytecode_disable() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, jvmClassName);


        disableBytecodeDumpService.dumpBytecode("disableTestDump", jvmClassName, classFile, classLoader);

        Mockito.verify(this.disassembler, times(0)).dumpBytecode(classFile);
        Mockito.verify(this.disassembler, times(0)).dumpVerify(classFile, classLoader);
        Mockito.verify(this.disassembler, times(0)).dumpASM(classFile);
    }


    @Test
    public void dumpBytecode_filter() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, jvmClassName);

        bytecodeDumpService.dumpBytecode("testDump", "invalidName", classFile, classLoader);

        Mockito.verify(this.disassembler, times(0)).dumpBytecode(classFile);
        Mockito.verify(this.disassembler, times(0)).dumpVerify(classFile, classLoader);
        Mockito.verify(this.disassembler, times(0)).dumpASM(classFile);

    }

}