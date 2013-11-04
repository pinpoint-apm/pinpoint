package com.nhn.pinpoint.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateSimpleInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.JDBCScope;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;

import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
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
            logger.info("Modifing. {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor interceptor = new ScopeDelegateSimpleInterceptor(new StatementExecuteQueryInterceptor(),JDBCScope.SCOPE);
            statementClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);

            // TODO 이거 고쳐야 됨.
            Interceptor executeUpdateInterceptor1 = new ScopeDelegateSimpleInterceptor(new StatementExecuteUpdateInterceptor(), JDBCScope.SCOPE);
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdateInterceptor1);

            Interceptor executeUpdateInterceptor2 = new ScopeDelegateSimpleInterceptor(new StatementExecuteUpdateInterceptor(), JDBCScope.SCOPE);
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "int"}, executeUpdateInterceptor2);

            Interceptor executeUpdateInterceptor3 = new ScopeDelegateSimpleInterceptor(new StatementExecuteUpdateInterceptor(), JDBCScope.SCOPE);
            statementClass.addInterceptor("execute", new String[]{"java.lang.String"}, executeUpdateInterceptor3);

            Interceptor executeUpdateInterceptor4 = new ScopeDelegateSimpleInterceptor(new StatementExecuteUpdateInterceptor(), JDBCScope.SCOPE);
            statementClass.addInterceptor("execute", new String[]{"java.lang.String", "int"}, executeUpdateInterceptor4);

            statementClass.addTraceVariable("__databaseInfo", "__setDatabaseInfo", "__getDatabaseInfo", "java.lang.Object");
            return statementClass.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }


}