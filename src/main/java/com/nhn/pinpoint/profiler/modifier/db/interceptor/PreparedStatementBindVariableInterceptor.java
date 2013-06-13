package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.interceptor.util.BindValueScope;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.DepthScope;
import com.nhn.pinpoint.profiler.util.MetaObject;
import com.nhn.pinpoint.profiler.util.NumberUtils;
import com.nhn.pinpoint.profiler.util.bindvalue.BindValueConverter;

import java.util.Map;
import com.nhn.pinpoint.profiler.logging.Logger;

public class PreparedStatementBindVariableInterceptor implements StaticAroundInterceptor, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(PreparedStatementBindVariableInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Map> getBindValue = new MetaObject<Map>("__getBindValue");
    private TraceContext traceContext;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        int push = BindValueScope.push();
        if (isDebug) {
            logger.debug("bindValueScope push:{}", push);
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {

        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }
        // mysql 드라이버 같은 경우는 내부 setObject가 다시 setXXX를 호출하는 구조라 scope 체크를 해야 됨.
        int pop = BindValueScope.pop();
        if (isDebug) {
            logger.debug("bindValueScope pop:{}", pop);
        }
        if (pop != DepthScope.ZERO) {
            logger.debug("internal bindValue scope. skip trace");
            return;
        }
        if (isDebug) {
            logger.afterInterceptor(target,className, methodName, parameterDescription, args, result);
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
