package com.nhn.pinpoint.profiler.modifier.db.mssql;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import javassist.CtClass;
import javassist.CtMethod;


import com.nhn.pinpoint.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.profiler.logging.Logger;

public class MSSQLResultSetModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(MSSQLResultSetModifier.class);

    public MSSQLResultSetModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/JtdsResultSet";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        return changeMethod(javassistClassName, classFileBuffer);
    }

    private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
        try {
            CtClass cc = null;

            updateNextMethod(cc);
            updateCloseMethod(cc);

            printClassConvertComplete(javassistClassName);

            return cc.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateNextMethod(CtClass cc) throws Exception {
        CtMethod serviceMethod1 = cc.getDeclaredMethod("next", null);
        serviceMethod1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".updateFetchCount(); }");
    }

    private void updateCloseMethod(CtClass cc) throws Exception {
        CtMethod serviceMethod1 = cc.getDeclaredMethod("close", null);
        serviceMethod1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".addResultSetData(); }");
    }
}