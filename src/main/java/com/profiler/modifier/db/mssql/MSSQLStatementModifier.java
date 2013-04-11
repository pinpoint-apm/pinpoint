package com.profiler.modifier.db.mssql;

import com.profiler.Agent;
import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

public class MSSQLStatementModifier extends AbstractModifier {
    private static final Logger logger = LoggerFactory.getLogger(MSSQLStatementModifier.class.getName());

    public MSSQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/JtdsStatement";
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

            updateExecuteQueryMethod(cc);

            printClassConvertComplete(javassistClassName);

            return null;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateExecuteQueryMethod(CtClass cc) throws Exception {
        CtClass[] params = new CtClass[1];
        params[0] = null;
        CtMethod serviceMethod = cc.getDeclaredMethod("executeQuery", params);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(DatabaseRequestTracer.FQCN + ".putSqlQuery(" + ProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$1);");
        sb.append(DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
        sb.append("}");

        serviceMethod.insertAfter(sb.toString());
    }
}
