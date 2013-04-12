package com.profiler.modifier.db.cubrid;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import com.profiler.logging.Logger;

public class CubridStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(CubridStatementModifier.class.getName());

    public CubridStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "cubrid/jdbc/driver/CUBRIDStatement";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        return changeMethod(javassistClassName, classFileBuffer);
    }

    private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {

        StaticAroundInterceptor interceptor = new StaticAroundInterceptor() {
            @Override
            public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
                DatabaseRequestTracer.putSqlQuery(ProfilerConstant.REQ_DATA_TYPE_DB_QUERY, (String) args[0]);
            }

            @Override
            public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
                DatabaseRequestTracer.put(ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY);
                ;
            }
        };

        try {
            // TODO  추가로 고쳐야 될듯.
            InstrumentClass aClass = this.byteCodeInstrumentor.getClass(javassistClassName);
            aClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);
            printClassConvertComplete(javassistClassName);
            CtClass cc = null;

            updateExecuteQueryMethod(cc);


            return cc.toBytecode();
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
        CtMethod method = cc.getDeclaredMethod("executeQuery", params);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(DatabaseRequestTracer.FQCN + ".putSqlQuery(" + ProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$1);");
        sb.append(DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
        sb.append("}");

        method.insertAfter(sb.toString());
    }
}
