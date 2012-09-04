package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.JavaAssistClass;
import javassist.*;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import javassist.bytecode.AccessFlag;

public class MySQLStatementModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MySQLStatementModifier.class.getName());

    public MySQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/StatementImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
//        classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

        InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
        Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteQueryMethodInterceptor");
        boolean executeQuery = aClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("executeQuery =" + executeQuery);
        }

        Interceptor interceptor1 = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteUpdateMethodInterceptor");
        boolean executeUpdate = aClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "boolean", "boolean"}, interceptor1);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("executeUpdate =" + executeUpdate);
        }
        // TODO 아무래도 에러 체크를 Exception으로 변경하는게 좋을것 같음.
        aClass.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.String");


        if (executeQuery && executeQuery) {
            return aClass.toBytecode();
        }
        return null;
    }


}