package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ASMBytecodeDumpService implements BytecodeDumpService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ENABLE_BYTECODE_DUMP = "bytecode.dump.enable";
    public static final boolean ENABLE_BYTECODE_DUMP_DEFAULT_VALUE = false;

    public static final String BYTECODE_DUMP_BYTECODE = "bytecode.dump.bytecode";
    public static final boolean BYTECODE_DUMP_BYTECODE_DEFAULT_VALUE = true;

    public static final String BYTECODE_DUMP_VERIFY = "bytecode.dump.verify";
    public static final boolean BYTECODE_DUMP_VERIFY_DEFAULT_VALUE = false;

    public static final String BYTECODE_DUMP_ASM = "bytecode.dump.asm";
    public static final boolean BYTECODE_DUMP_ASM_DEFAULT_VALUE = true;

    public static final String DUMP_CLASS_LIST = "bytecode.dump.classlist";

    private final boolean dumpBytecode;
    private final boolean dumpVerify;
    private final boolean dumpASM;
    private final Set<String> dumpClassInternalNameSet;

    private ASMBytecodeDisassembler disassembler = new ASMBytecodeDisassembler();

    public ASMBytecodeDumpService(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

        this.dumpBytecode = profilerConfig.readBoolean(BYTECODE_DUMP_BYTECODE, BYTECODE_DUMP_BYTECODE_DEFAULT_VALUE);
        this.dumpVerify = profilerConfig.readBoolean(BYTECODE_DUMP_VERIFY, BYTECODE_DUMP_VERIFY_DEFAULT_VALUE);
        this.dumpASM = profilerConfig.readBoolean(BYTECODE_DUMP_ASM, BYTECODE_DUMP_ASM_DEFAULT_VALUE);

        this.dumpClassInternalNameSet = getClassName(profilerConfig);
    }

    private Set<String> getClassName(ProfilerConfig profilerConfig) {
        final String classNameList = profilerConfig.readString(DUMP_CLASS_LIST, "");
        if (classNameList.isEmpty()) {
            return Collections.emptySet();
        } else {
            final List<String> classList = StringUtils.tokenizeToStringList(classNameList, ",");
            final List<String> classInternalNameList = toInternalNames(classList);
            return new HashSet<String>(classInternalNameList);
        }
    }

    public ASMBytecodeDumpService(boolean dumpBytecode, boolean dumpVerify, boolean dumpASM, List<String> classNameList) {
        if (classNameList == null) {
            throw new NullPointerException("classNameList must not be null");
        }

        this.dumpBytecode = dumpBytecode;
        this.dumpVerify = dumpVerify;
        this.dumpASM = dumpASM;

        List<String> classInternalNameList = toInternalNames(classNameList);
        this.dumpClassInternalNameSet = new HashSet<String>(classInternalNameList);
    }

    private List<String> toInternalNames(List<String> classNameList) {
        List<String> classInternalNameList = new ArrayList<String>(classNameList.size());

        for (String className : classNameList) {
            classInternalNameList.add(JavaAssistUtils.javaNameToJvmName(className));
        }
        return classInternalNameList;
    }

    @Override
    public void dumpBytecode(String dumpMessage, final String classInternalName, final byte[] bytes, ClassLoader classLoader) {
        if (classInternalName == null) {
            throw new NullPointerException("classInternalName must not be null");
        }

        if (!filterClassName(classInternalName)) {
            return;
        }


        if (dumpBytecode) {
            final String dumpBytecode = this.disassembler.dumpBytecode(bytes);
            logger.info("{} class:{} bytecode:{}", dumpMessage, classInternalName, dumpBytecode);
        }

        if (dumpVerify) {
            if (classLoader == null) {
                logger.debug("classLoader is null, classInternalName:{}", classInternalName);
            }
            classLoader = getClassLoader(classLoader);
            final String dumpVerify = this.disassembler.dumpVerify(bytes, classLoader);
            logger.info("{} class:{} verify:{}", dumpMessage, classInternalName, dumpVerify);
        }

        if (dumpASM) {
            final String dumpASM = this.disassembler.dumpASM(bytes);
            logger.info("{} class:{} asm:{}", dumpMessage, classInternalName, dumpASM);
        }
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }

    private boolean filterClassName(String classInternalName) {
        return this.dumpClassInternalNameSet.contains(classInternalName);
    }
}

