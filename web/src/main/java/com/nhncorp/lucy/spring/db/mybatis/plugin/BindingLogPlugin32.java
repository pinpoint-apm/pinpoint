package com.nhncorp.lucy.spring.db.mybatis.plugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.Assert;

import com.nhncorp.lucy.spring.db.mybatis.plugin.util.DefaultBindingLogFormatter;
import com.nhncorp.lucy.spring.db.mybatis.plugin.util.PreparedStatementParameterLogger;

/**
 * Binding Log 출력 Plugin
 * <p>
 * {@link java.sql.PreparedStatement}, {@link java.sql.CallableStatement} 의 바인딩 변수를 Query와 같이 출력해 주는 plugin.
 * </p>
 * Query문의 format을 {@link BindLogFormatter}을 통해 변경할 수 있다.
 * 기본구현체는 {@link com.nhncorp.lucy.spring.db.mybatis.plugin.util.DefaultBindingLogFormatter} 이다.
 * 공백 제거 옵션인 removeWhitespace옵션이 지원된다.
 *
 * @author Web Platform Development Lab
 * @author emeroad
 * @see Interceptor
 * @see BindLogFormatter
 * @see com.nhncorp.lucy.spring.db.mybatis.plugin.util.DefaultBindingLogFormatter
 * @since 1.7.4
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})}
)
public class BindingLogPlugin32 implements Interceptor {

    private static final Log pLogger = LogFactory.getLog(PreparedStatement.class);
    private static final Log internalLogger = LogFactory.getLog(BindingLogPlugin32.class);

    private BindLogFormatter bindLogFormatter;

    public BindingLogPlugin32() {
        this.bindLogFormatter = new DefaultBindingLogFormatter();
    }

    public BindingLogPlugin32(BindLogFormatter bindLogFormatter) {
        Assert.notNull(bindLogFormatter, "bindLogFormatter must no be null");
        this.bindLogFormatter = bindLogFormatter;
    }


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (internalLogger.isTraceEnabled()) {
            internalLogger.trace("target:" + invocation.getTarget()
                    + " method:" + invocation.getMethod() + " args:" + Arrays.toString(invocation.getArgs()));
        }
        try {
            return invocation.proceed();
        } finally {
            bindingLog(invocation);
        }
    }

    private void bindingLog(Invocation invocation) throws SQLException {

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        StatementType statementType = ms.getStatementType();
        if (StatementType.PREPARED == statementType || StatementType.CALLABLE == statementType) {
            Log statementLog = ms.getStatementLog();
            if (isDebugEnable(statementLog)) {
                BoundSql boundSql = ms.getBoundSql(parameterObject);

                String sql = boundSql.getSql();
                List<String> parameterList = getParameters(ms, parameterObject, boundSql);
                debug(statementLog, "==> BindingLog: " + bindLogFormatter.format(sql, parameterList));
            }
        }
    }

    public boolean isDebugEnable(Log statementLogger) {
        return statementLogger.isDebugEnabled() || pLogger.isDebugEnabled();
    }

    public void debug(Log statementLogger, String msg) {
        if (statementLogger.isDebugEnabled()) {
            statementLogger.debug(msg);
        } else {
            pLogger.debug(msg);
        }
    }

    private List<String> getParameters(MappedStatement ms, Object parameterObject, BoundSql boundSql) throws SQLException {
        // 현재 parameterHandler의 구현체는 DefaultParameterHandler가 유일함 추후 변경될수 있음
        // 이경우 구현체를 탐색하는 추가 코드가 필요함.
        ParameterHandler parameterHandler = new DefaultParameterHandler(ms, parameterObject, boundSql);
        PreparedStatementParameterLogger parameterLogger = new PreparedStatementParameterLogger();
        parameterHandler.setParameters(parameterLogger);
        return parameterLogger.getParameters();
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * {@link BindLogFormatter}의 구현체인 {@link com.nhncorp.lucy.spring.db.mybatis.plugin.util.DefaultBindingLogFormatter} 는 removeWhitespace 옵션을 지원한다.
     * removeWhitespace : query 포멧을 한줄로 출력되도록 설정한다.
     *
     * @param properties 옵션 properties
     */
    @Override
    public void setProperties(Properties properties) {
        bindLogFormatter.setProperties(properties);
    }

    public void setBindLogFormatter(BindLogFormatter bindLogFormatter) {
        Assert.notNull(bindLogFormatter, "bindLogFormatter must no be null");
        this.bindLogFormatter = bindLogFormatter;
    }
}