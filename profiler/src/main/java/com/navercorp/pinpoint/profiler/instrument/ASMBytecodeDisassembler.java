package com.navercorp.pinpoint.profiler.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ASMBytecodeDisassembler {

    private final int cwFlag;
    private final int crFlag;

    public ASMBytecodeDisassembler() {
        this(0, 0);
    }

    public ASMBytecodeDisassembler(int crFlag, int cwFlag) {
        this.cwFlag = cwFlag;
        this.crFlag = crFlag;
    }

    public String dumpBytecode(final byte[] bytecode) {
        if (bytecode == null) {
            throw new NullPointerException("bytecode");
        }

        return writeBytecode(bytecode, new Textifier());
    }


    public String dumpASM(byte[] bytecode) {
        if (bytecode == null) {
            throw new NullPointerException("bytecode");
        }

        return writeBytecode(bytecode, new ASMifier());
    }

    private String writeBytecode(byte[] bytecode, Printer printer) {

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);

        accept(bytecode, printer, writer);

        return out.toString();
    }

    private void accept(byte[] bytecode, Printer printer, PrintWriter writer) {

        final ClassReader cr = new ClassReader(bytecode);
        final ClassWriter cw = new ClassWriter(this.cwFlag);
        final TraceClassVisitor tcv = new TraceClassVisitor(cw, printer, writer);
        cr.accept(tcv, this.crFlag);
    }

    public String dumpVerify(byte[] bytecode, ClassLoader classLoader) {
        if (bytecode == null) {
            throw new NullPointerException("bytecode");
        }
        if (classLoader == null) {
            throw new NullPointerException("classLoader");
        }

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);

        final ClassReader cr = new ClassReader(bytecode);
        CheckClassAdapter.verify(cr, classLoader, true, writer);

        return out.toString();
    }


}
