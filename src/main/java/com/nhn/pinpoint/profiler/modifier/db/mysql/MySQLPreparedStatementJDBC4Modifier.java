package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateStaticInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.*;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.JDBCScope;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.nhn.pinpoint.profiler.util.BindVariableFilter;
import com.nhn.pinpoint.profiler.util.IncludeBindVariableFilter;
import com.nhn.pinpoint.profiler.util.JavaAssistUtils;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

/**
 * @author emeroad
 */
public class MySQLPreparedStatementJDBC4Modifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLPreparedStatementJDBC4Modifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/JDBC4PreparedStatement";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + className);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, className);
        try {
            InstrumentClass preparedStatement = byteCodeInstrumentor.getClass(className);

            bindVariableIntercept(preparedStatement, classLoader, protectedDomain);

            return preparedStatement.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        // TODO 문자열에 추가로 파라미터 type을 넣어야 될거 같음.
        // jdbc 드라이버 마다 구현api가 약간식 차이가 있는데 파라미터 타입이 없을경우, api 판별에 한계가 있음.
        BindVariableFilter exclude = new IncludeBindVariableFilter(new String[]{"setRowId", "setNClob", "setSQLXML"});
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod(exclude);
        // TODO 해당 로직 공통화 필요?
        // bci 쪽에 multi api 스펙에 대한 자동으로 인터셉터를 n개 걸어주는 api가 더 좋지 않을까한다.
        Interceptor interceptor = new ScopeDelegateStaticInterceptor(new PreparedStatementBindVariableInterceptor(), JDBCScope.SCOPE);
        int interceptorId = -1;
        for (Method method : bindMethod) {
            String methodName = method.getName();
            String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
            try {
                if (interceptorId == -1) {
                    interceptorId = preparedStatement.addInterceptor(methodName, parameterType, interceptor, Type.after);
                } else {
                    preparedStatement.reuseInterceptor(methodName, parameterType, interceptorId, Type.after);
                }
            } catch (NotFoundInstrumentException e) {
                // bind variable setter메소드를 못찾을 경우는 그냥 경고만 표시, 에러 아님.
                // stack trace는 일부러 안찍음.
                if (logger.isDebugEnabled()) {
                    logger.debug("bindVariable api not found. method:{} param:{} Cause:{}", methodName, Arrays.toString(parameterType), e.getMessage());
                }
            }
        }
    }
}
