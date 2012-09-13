package com.profiler.modifier.db.mysql;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.interceptor.bci.NotFoundInstrumentException;
import com.profiler.modifier.AbstractModifier;
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
        BindVariableFilter exclude = new IncludeBindVariableFilter(includes);
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod(exclude);
        Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.PreparedStatementBindVariableInterceptor");
        for (Method method : bindMethod) {
            String methodName = method.getName();
            String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
            try {
                preparedStatement.addInterceptor(methodName, parameterType, interceptor);
            } catch (NotFoundInstrumentException e) {
                // bind variable setter메소드를 못찾을 경우는 그냥 경고만 표시, 에러 아님.
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "bindVariable api not found. Cause:" + e.getMessage(), e);
                }
            }
        }
    }
}
