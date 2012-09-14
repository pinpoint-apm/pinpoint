package com.profiler.modifier.db.mysql;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.interceptor.bci.NotFoundInstrumentException;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.profiler.util.*;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLPreparedStatementJDBC4Modifier extends AbstractModifier  {

	private final Logger logger = Logger.getLogger(MySQLPreparedStatementJDBC4Modifier.class.getName());
    private final String[] includes = new String[] { "setRowId", "setNClob", "setSQLXML" };

	public MySQLPreparedStatementJDBC4Modifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

    public String getTargetClass() {
        return "com/mysql/jdbc/JDBC4PreparedStatement";
	}

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + className);
		}
        checkLibrary(classLoader, className);
        try {
            InstrumentClass preparedStatement = byteCodeInstrumentor.getClass(className);

            bindVariableIntercept(preparedStatement, classLoader, protectedDomain);

            return preparedStatement.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }
    }

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        // TODO 문자열에 추가로 파라미터 type을 넣어야 될거 같음.
        // jdbc 드라이버 마다 구현api가 약간식 차이가 있는데 파라미터 타입이 없을경우, api 판별에 한계가 있음.
        BindVariableFilter exclude = new IncludeBindVariableFilter(includes);
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod(exclude);
        // TODO 해당 로직 공통화 필요?
        // bci 쪽에 multi api 스펙에 대한 자동으로 인터셉터를 n개 걸어주는 api가 더 좋지 않을까한다.
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
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "bindVariable api not found. Cause:" + e.getMessage(), e);
                }
            }
        }
    }
}
