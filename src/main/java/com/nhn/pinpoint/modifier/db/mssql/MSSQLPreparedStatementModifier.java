package com.nhn.pinpoint.modifier.db.mssql;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConstant;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.modifier.AbstractModifier;
import com.nhn.pinpoint.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.profiler.logging.Logger;

public class MSSQLPreparedStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(MSSQLPreparedStatementModifier.class);

    public MSSQLPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/JtdsPreparedStatement";
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

            updateSetParameterMethod(cc);
            updateExecuteQueryMethod(cc);
            updateConstructor(cc);

            printClassConvertComplete(javassistClassName);

            return cc.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateSetParameterMethod(CtClass cc) throws Exception {
        CtClass[] params1 = new CtClass[5];
        params1[0] = null;
        params1[1] = null;
        params1[2] = null;
        params1[3] = null;
        params1[4] = null;
        CtMethod method = cc.getDeclaredMethod("setParameter", params1);

        method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2);} ");
    }

    private void updateConstructor(CtClass cc) throws Exception {
        CtConstructor[] constructorList = cc.getConstructors();

        if (constructorList.length == 1) {
            CtConstructor constructor = constructorList[0];
            constructor.insertAfter("{" + DatabaseRequestTracer.FQCN + ".putSqlQuery(" + ProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
        }
    }

    private void updateExecuteQueryMethod(CtClass cc) throws Exception {
        CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
        serviceMethod.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
    }
}
