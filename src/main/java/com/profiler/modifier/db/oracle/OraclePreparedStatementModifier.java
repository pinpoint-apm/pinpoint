package com.profiler.modifier.db.oracle;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import com.profiler.logging.Logger;

public class OraclePreparedStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(OraclePreparedStatementModifier.class);

    public OraclePreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "oracle/jdbc/driver/OraclePreparedStatement";
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
            updateSetInternalMethod(cc);
            updateExecuteMethod(cc);
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

    private void updateSetInternalMethod(CtClass cc) throws Exception {
        CtClass[] params1 = new CtClass[2];
        params1[0] = null;
        params1[1] = null;
        CtMethod serviceMethod1 = cc.getDeclaredMethod("setStringInternal", params1);

        serviceMethod1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2); }");

        // CtClass[] params2 = new CtClass[2];
        // params2[0] = classPool.getCtClass("int");
        // params2[1] = classPool.getCtClass("byte[]");
        // CtMethod serviceMethod2 = cc.getDeclaredMethod("setInternal",
        // params2);
        //
        // serviceMethod2.insertBefore("{" +
        // RequestDataTracer.FQCN +
        // ".putSqlParam($1,$2); {");
    }

    private void updateConstructor(CtClass cc) throws Exception {
        CtConstructor[] constructorList = cc.getConstructors();

        for (CtConstructor constructor : constructorList) {
            CtClass params[] = constructor.getParameterTypes();
            if (params.length == 6) {
                constructor.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlQuery(" + ProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
            }
        }
    }

    private void updateExecuteMethod(CtClass cc) throws Exception {
        CtMethod method = cc.getDeclaredMethod("execute", null);
        method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
    }
}
