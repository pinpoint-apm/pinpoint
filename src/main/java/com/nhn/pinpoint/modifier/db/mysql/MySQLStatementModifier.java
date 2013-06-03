package com.nhn.pinpoint.modifier.db.mysql;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;
import com.nhn.pinpoint.logging.LoggerFactory;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.modifier.db.interceptor.StatementExecuteQueryInterceptor;

import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.modifier.AbstractModifier;

public class MySQLStatementModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/StatementImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(javassistClassName);
            Interceptor interceptor = new StatementExecuteQueryInterceptor();
            statementClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);

            // TODO 이거 고쳐야 됨.
            Interceptor executeUpdate1 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdate1);
            Interceptor executeUpdate2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "int"}, executeUpdate2);

            Interceptor executeUpdate3 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("execute", new String[]{"java.lang.String"}, executeUpdate3);
            Interceptor executeUpdate4 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
            statementClass.addInterceptor("execute", new String[]{"java.lang.String", "int"}, executeUpdate4);

            statementClass.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");
            return statementClass.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }
    }


}