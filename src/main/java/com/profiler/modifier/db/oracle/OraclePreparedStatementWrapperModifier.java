package com.profiler.modifier.db.oracle;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.interceptor.bci.NotFoundInstrumentException;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.profiler.modifier.db.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.profiler.util.JavaAssistUtils;
import com.profiler.util.PreparedStatementUtils;


import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.List;

public class OraclePreparedStatementWrapperModifier extends AbstractModifier {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OraclePreparedStatementWrapperModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "oracle/jdbc/driver/OraclePreparedStatementWrapper";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass preparedStatement = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor execute = new PreparedStatementExecuteQueryInterceptor();
            preparedStatement.addInterceptor("execute", null, execute);
            Interceptor executeQuery = new PreparedStatementExecuteQueryInterceptor();
            preparedStatement.addInterceptor("executeQuery", null, executeQuery);
            Interceptor executeUpdate = new PreparedStatementExecuteQueryInterceptor();
            preparedStatement.addInterceptor("executeUpdate", null, executeUpdate);

            preparedStatement.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");
            preparedStatement.addTraceVariable("__sql", "__setSql", "__getSql", "java.lang.Object");

            preparedStatement.addTraceVariable("__bindValue", "__setBindValue", "__getBindValue", "java.util.Map", "java.util.Collections.synchronizedMap(new java.util.HashMap());");
            bindVariableIntercept(preparedStatement, classLoader, protectedDomain);

            return preparedStatement.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }

    }

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod();

        Interceptor interceptor = new PreparedStatementBindVariableInterceptor();
        int interceptorId = -1;
        for (Method method : bindMethod) {
            String methodName = method.getName();
            String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
            try {
                if (interceptorId == -1) {
                    interceptorId = preparedStatement.addInterceptor(methodName, parameterType, interceptor);
                } else {
                    preparedStatement.reuseInterceptor(methodName, parameterType, interceptorId);
                }
            } catch (NotFoundInstrumentException e) {
                // bind variable setter메소드를 못찾을 경우는 그냥 경고만 표시, 에러 아님.
                if (logger.isTraceEnabled()) {
                    logger.trace("bindVariable api not found. Cause:" + e.getMessage(), e);
                }
            }
        }

    }

}
