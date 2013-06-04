package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConstant;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.profiler.logging.Logger;

public class CubridPreparedStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(CubridPreparedStatementModifier.class.getName());

    public CubridPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "cubrid/jdbc/driver/CUBRIDPreparedStatement";
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

    private void updateConstructor(CtClass cc) throws Exception {
        CtConstructor[] constructorList = cc.getConstructors();

        for (CtConstructor constructor : constructorList) {
            CtClass params[] = constructor.getParameterTypes();

            if (params.length > 2) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("if($2 instanceof cubrid.jdbc.jci.UStatement) { ");
                sb.append(DatabaseRequestTracer.FQCN + ".putSqlQuery(" + ProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2.getQuery());");
                sb.append("}}");

                constructor.insertBefore(sb.toString());
            }
        }
    }

    private void updateExecuteQueryMethod(CtClass cc) throws Exception {
        CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
        serviceMethod.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
    }
}
