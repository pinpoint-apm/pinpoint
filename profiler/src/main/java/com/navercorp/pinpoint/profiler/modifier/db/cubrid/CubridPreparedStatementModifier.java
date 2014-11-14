package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.Scope;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.BindValueTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ParsingResultTraceValue;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateStaticInterceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.*;
import com.nhn.pinpoint.profiler.util.JavaAssistUtils;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class CubridPreparedStatementModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	public CubridPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDPreparedStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}
		try {
			InstrumentClass preparedStatementClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            Interceptor executeInterceptor = new PreparedStatementExecuteQueryInterceptor();
            preparedStatementClass.addScopeInterceptor("execute", null, executeInterceptor, CubridScope.SCOPE_NAME);

            Interceptor executeQueryInterceptor = new PreparedStatementExecuteQueryInterceptor();
            preparedStatementClass.addScopeInterceptor("executeQuery", null, executeQueryInterceptor, CubridScope.SCOPE_NAME);

            Interceptor executeUpdateInterceptor = new PreparedStatementExecuteQueryInterceptor();
            preparedStatementClass.addScopeInterceptor("executeUpdate", null, executeUpdateInterceptor, CubridScope.SCOPE_NAME);

            preparedStatementClass.addTraceValue(DatabaseInfoTraceValue.class);
            preparedStatementClass.addTraceValue(ParsingResultTraceValue.class);
            preparedStatementClass.addTraceValue(BindValueTraceValue.class, "new java.util.HashMap();");

			bindVariableIntercept(preparedStatementClass, classLoader, protectedDomain);

			return preparedStatementClass.toBytecode();
		} catch (InstrumentException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
			}
			return null;
		}
	}

	private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
		List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod();
        final Scope scope = byteCodeInstrumentor.getScope(CubridScope.SCOPE_NAME);
        Interceptor interceptor = new ScopeDelegateStaticInterceptor(new PreparedStatementBindVariableInterceptor(), scope);
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
                if (logger.isDebugEnabled()) {
                    logger.debug("bindVariable api not found. method:{} param:{} Cause:{}", methodName, Arrays.toString(parameterType), e.getMessage());
                }
			}
		}
	}
}
