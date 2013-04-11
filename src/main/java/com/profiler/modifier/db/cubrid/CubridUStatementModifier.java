package com.profiler.modifier.db.cubrid;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.logging.LoggerFactory;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import com.profiler.logging.Logger;

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
