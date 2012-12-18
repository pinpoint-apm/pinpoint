package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

public class MySQLStatementModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MySQLStatementModifier.class.getName());

    public MySQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/StatementImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(javassistClassName);
            Interceptor interceptor = new StatementExecuteQueryInterceptor();
            statementClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);

            // TODO 이거 고쳐야 됨.
            Interceptor executeUpdate1 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdate1);
            Interceptor executeUpdate2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "boolean"}, executeUpdate2);

            Interceptor executeUpdate3 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("execute", new String[]{"java.lang.String"}, executeUpdate3);
            Interceptor executeUpdate4 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("execute", new String[]{"java.lang.String", "boolean"}, executeUpdate4);

            statementClass.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");
            return statementClass.toBytecode();
        } catch (InstrumentException e) {
            return null;
        }
    }


}