package com.nhn.pinpoint.modifier.db.mysql;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;

import com.nhn.pinpoint.modifier.AbstractModifier;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

// TODO 추가 개발해야 될듯.
public class MySQLResultSetModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLResultSetModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/ResultSetImpl";
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
//            CtClass cc = null;

//            updateNextMethod(cc);
//            updateCloseMethod(cc);

//            printClassConvertComplete(javassistClassName);

            return null;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

//    private void updateNextMethod(CtClass cc) throws Exception {
//        CtMethod method = cc.getDeclaredMethod("next", null);
//        method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".updateFetchCount(); }");
//    }
//
//    private void updateCloseMethod(CtClass cc) throws Exception {
//        CtMethod method = cc.getDeclaredMethod("close", null);
//        method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".addResultSetData(); }");
//    }
}
