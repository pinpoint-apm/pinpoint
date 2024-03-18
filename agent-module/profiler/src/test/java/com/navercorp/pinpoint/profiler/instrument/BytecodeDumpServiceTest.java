package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.never;

/**
 * @author Woonduk Kang(emeroad)
 */
@ExtendWith(MockitoExtension.class)
public class BytecodeDumpServiceTest {

    private final String classInternalName = JavaAssistUtils.javaNameToJvmName("java.lang.String");

    @Mock
    private ASMBytecodeDisassembler disassembler;

    @InjectMocks
    private BytecodeDumpService bytecodeDumpService = new ASMBytecodeDumpService(true, true, true, Collections.singletonList(classInternalName));

    @InjectMocks
    private BytecodeDumpService disableBytecodeDumpService = new ASMBytecodeDumpService(false, false, false, Collections.singletonList(classInternalName));

    @Test
    public void dumpBytecode() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, classInternalName);


        bytecodeDumpService.dumpBytecode("testDump", classInternalName, classFile, classLoader);

        Mockito.verify(this.disassembler).dumpBytecode(classFile);
        Mockito.verify(this.disassembler).dumpVerify(classFile, classLoader);
        Mockito.verify(this.disassembler).dumpASM(classFile);
    }


    @Test
    public void dumpBytecode_disable() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, classInternalName);


        disableBytecodeDumpService.dumpBytecode("disableTestDump", classInternalName, classFile, classLoader);

        Mockito.verify(this.disassembler, never()).dumpBytecode(classFile);
        Mockito.verify(this.disassembler, never()).dumpVerify(classFile, classLoader);
        Mockito.verify(this.disassembler, never()).dumpASM(classFile);
    }


    @Test
    public void dumpBytecode_filter() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        byte[] classFile = BytecodeUtils.getClassFile(classLoader, classInternalName);

        bytecodeDumpService.dumpBytecode("testDump", "invalidName", classFile, classLoader);

        Mockito.verify(this.disassembler, never()).dumpBytecode(classFile);
        Mockito.verify(this.disassembler, never()).dumpVerify(classFile, classLoader);
        Mockito.verify(this.disassembler, never()).dumpASM(classFile);

    }

}