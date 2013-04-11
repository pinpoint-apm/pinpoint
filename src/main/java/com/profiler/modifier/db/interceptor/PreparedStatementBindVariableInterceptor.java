package com.profiler.modifier.db.interceptor;

import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.interceptor.util.JDBCScope;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.NumberUtils;
import com.profiler.util.bindvalue.BindValueConverter;

import java.util.Map;
import java.util.logging.Level;
import com.profiler.logging.Logger;

public class PreparedStatementBindVariableInterceptor implements StaticAfterInterceptor, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(PreparedStatementBindVariableInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Map> getBindValue = new MetaObject<Map>("__getBindValue");
    private TraceContext traceContext;

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }
        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        Map bindList = getBindValue.invoke(target);
        if (bindList == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("bindValue is null");
            }
            return;
        }
        Integer index = NumberUtils.toInteger(args[0]);
        if (index == null) {
            // 어딘가 잘못됨.
            return;
        }
        String value = BindValueConverter.convert(methodName, args);
        bindList.put(index, value);

    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
