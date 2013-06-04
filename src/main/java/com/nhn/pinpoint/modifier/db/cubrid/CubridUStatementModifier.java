package com.nhn.pinpoint.modifier.db.cubrid;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import javassist.CtClass;
import javassist.CtMethod;

import com.nhn.pinpoint.modifier.AbstractModifier;
import com.nhn.pinpoint.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.profiler.logging.Logger;

public class CubridUStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(CubridUStatementModifier.class.getName());

    public CubridUStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "cubrid/jdbc/jci/UStatement";
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

            updateBindValueMethod(cc);

            printClassConvertComplete(javassistClassName);

            return cc.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateBindValueMethod(CtClass cc) throws Exception {
        CtClass[] params1 = new CtClass[3];
        params1[0] = null;
        params1[1] = null;
        params1[2] = null;
        CtMethod method = cc.getDeclaredMethod("bindValue", params1);

        method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$3); }");
    }
}
