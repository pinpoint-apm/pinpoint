package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import javassist.CtClass;
import javassist.CtMethod;

import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.profiler.logging.Logger;

public class CubridResultSetModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(CubridResultSetModifier.class.getName());

    public CubridResultSetModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "cubrid/jdbc/driver/CUBRIDResultSet";
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
        CtMethod method = cc.getDeclaredMethod("next", null);
        method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".updateFetchCount(); }");
    }

    private void updateCloseMethod(CtClass cc) throws Exception {
        CtMethod method = cc.getDeclaredMethod("close", null);
        method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".addResultSetData(); } ");
    }
}
