package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConstant;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.NotFoundInstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.nhn.pinpoint.profiler.trace.DatabaseRequestTracer;
import com.nhn.pinpoint.profiler.util.ExcludeBindVariableFilter;
import com.nhn.pinpoint.profiler.util.JavaAssistUtils;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;
import javassist.CtClass;
import javassist.CtConstructor;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.List;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

public class MySQLPreparedStatementModifier extends AbstractModifier {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String[] excludes = new String[]{"setRowId", "setNClob", "setSQLXML"};

    public MySQLPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/PreparedStatement";
        // 상속관계일 경우 byte코드를 수정할 객체를 타겟으로해야 됨.
//        return "com/mysql/jdbc/JDBC4PreparedStatement";
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

//		Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.mysql.interceptor.ExecuteMethodInterceptor");
//		if (interceptor == null) {
//			return null;
//		}
//
//		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
//
//		InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
//		aClass.addInterceptor("executeQuery", null, interceptor);

//		return changeMethod(javassistClassName, classFileBuffer);
    }

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        ExcludeBindVariableFilter exclude = new ExcludeBindVariableFilter(excludes);
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod(exclude);

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
