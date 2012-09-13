package com.profiler.modifier.db.mysql;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.db.mysql.interceptors.*;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnectionImplModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MySQLConnectionImplModifier.class.getName());

	public MySQLConnectionImplModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "com/mysql/jdbc/ConnectionImpl";
	}
	
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)){
		    logger.info("Modifing. " + javassistClassName);
        }
		checkLibrary(classLoader, javassistClassName);
		try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);


            mysqlConnection.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.String");

            // 해당 Interceptor를 공통클래스 만들경우 system에 로드해야 된다.
//            Interceptor createConnection = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.CreateConnectionInterceptor");
            Interceptor createConnection  = new CreateConnectionInterceptor();
            String[] params = new String[] {
                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
            };
            mysqlConnection.addInterceptor("getInstance", params, createConnection);


//            Interceptor closeConnection = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.CloseConnectionInterceptor");
            Interceptor closeConnection = new CloseConnectionInterceptor();
            mysqlConnection.addInterceptor("close", null, closeConnection);


//            Interceptor createStatement = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.CreateStatementInterceptor");
            Interceptor createStatement = new CreateStatementInterceptor();
            mysqlConnection.addInterceptor("createStatement", null, createStatement);


//            Interceptor preparedStatement = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.CreatePreparedStatementInterceptor");
            Interceptor preparedStatement = new CreatePreparedStatementInterceptor();
            mysqlConnection.addInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatement);


//            Interceptor transaction = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.c");
            Interceptor transaction = new TransactionInterceptor();
            mysqlConnection.addInterceptor("setAutoCommit", new String[]{"boolean"}, transaction);
            mysqlConnection.addInterceptor("commit", null, transaction);
            mysqlConnection.addInterceptor("rollback", null, transaction);

			printClassConvertComplete(javassistClassName);

			return mysqlConnection.toBytecode();
		} catch (InstrumentException e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
		}
	}


//	private void updateCreateStatementMethod(CtClass cc) throws Exception {
//		CtMethod method = cc.getDeclaredMethod("createStatement", null);
//		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + "); }");
//	}
//
//	private void updateGetInstanceMethod(CtClass cc) throws Exception {
//		CtClass[] params = new CtClass[5];
//		params[0] = classPool.getCtClass("java.lang.String");
//		params[1] = classPool.getCtClass("int");
//		params[2] = classPool.getCtClass("java.util.Properties");
//		params[3] = classPool.getCtClass("java.lang.String");
//		params[4] = classPool.getCtClass("java.lang.String");
//		CtMethod method = cc.getDeclaredMethod("getInstance", params);
//
//		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$5); }");
//	}
//
//	private void updateCloseMethod(CtClass cc) throws Exception {
//		CtMethod method = cc.getDeclaredMethod("close", null);
//		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
//	}
}
