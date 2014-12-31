/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.ibatis;

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

/**
 * Plugin for printing out the bind variables of {@link java.sql.PreparedStatement} and {@link java.sql.CallableStatement} with Query string.
 * format of Query string can be changed with {@link BindLogFormatter}.
 * base implementation is {@link com.navercorp.pinpoint.web.dao.ibatis.DefaultBindingLogFormatter}.
 * removeWhitespace option is supported
 *
 * @author emeroad
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
        // DefaultParameterHandler is the only implementation of parameterHandler interface currently. it may be changed later.
        // need additional codes to find a appropriate implementation in that case.
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
     * {@link com.navercorp.pinpoint.web.dao.ibatis.DefaultBindingLogFormatter} is the implementation of {@link BindLogFormatter} supports removeWhitespace option.
     * removeWhitespace : setting for printing out query format in a row.
     *
     * @param properties option properties
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