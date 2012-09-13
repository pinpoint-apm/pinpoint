package com.profiler.modifier.db.mysql;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.interceptor.bci.NotFoundInstrumentException;
import com.profiler.util.ExcludeBindVariableFilter;
import com.profiler.util.JavaAssistUtils;
import com.profiler.util.PreparedStatementUtils;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

public class MySQLPreparedStatementModifier extends AbstractModifier {
	private final Logger logger = Logger.getLogger(MySQLPreparedStatementModifier.class.getName());
    private final String[] excludes = new String[] { "setRowId", "setNClob", "setSQLXML" };

	public MySQLPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "com/mysql/jdbc/PreparedStatement";
        // 상속관계일 경우 byte코드를 수정할 객체를 타겟으로해야 됨.
//        return "com/mysql/jdbc/JDBC4PreparedStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass preparedStatement = byteCodeInstrumentor.getClass(javassistClassName);
            Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.PreparedStatementMethodInterceptor");
            preparedStatement.addInterceptor("executeQuery", null, interceptor);

            preparedStatement.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.String");
            preparedStatement.addTraceVariable("__sql", "__setSql", "__getSql", "java.lang.String");

            preparedStatement.addTraceVariable("__bindValue", "__setBindValue", "__getBindValue", "java.util.Map", "java.util.Collections.synchronizedMap(new java.util.HashMap());");
            bindVariableIntercept(preparedStatement, classLoader, protectedDomain);

            return preparedStatement.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }

//		Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteMethodInterceptor");
//		if (interceptor == null) {
//			return null;
//		}
//
//		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
//		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));
//
//		InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
//		aClass.addInterceptor("executeQuery", null, interceptor);

//		return changeMethod(javassistClassName, classFileBuffer);
	}

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        ExcludeBindVariableFilter exclude = new ExcludeBindVariableFilter(excludes);
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


    private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateSetInternalMethod(cc);
			updateExecuteQueryMethod(cc);
			updateConstructor(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
		}
		return null;
	}

	private void updateSetInternalMethod(CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[2];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.String");
		CtMethod method1 = cc.getDeclaredMethod("setInternal", params1);

		method1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2); }");

		CtClass[] params2 = new CtClass[2];
		params2[0] = classPool.getCtClass("int");
		params2[1] = classPool.getCtClass("byte[]");
		CtMethod method2 = cc.getDeclaredMethod("setInternal", params2);

		method2.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2); }");
	}

	private void updateConstructor(CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		if (constructorList.length == 3) {
			for (CtConstructor constructor : constructorList) {
				CtClass params[] = constructor.getParameterTypes();
				if (params.length == 3) {
					constructor.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
				}
			}
		}
	}

	private void updateExecuteQueryMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("executeQuery", null);
		method.insertAfter("{System.out.println(\"EXECUTE QUERY\");" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
